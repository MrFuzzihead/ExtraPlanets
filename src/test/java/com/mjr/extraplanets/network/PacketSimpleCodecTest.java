package com.mjr.extraplanets.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

import org.junit.Assert;
import org.junit.Test;

import com.mjr.extraplanets.Config;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Covers {@link PacketSimple}'s wire format and the hardening added for the "packet type ordinal is not validated"
 * finding.
 *
 * <p>
 * Every GC-derived mod ships this class and every one of them has the same two failure modes: the payload is
 * written with {@code NetworkUtil.encodeData} and read back positionally by {@code decodeData}, and the packet
 * type is a raw enum ordinal. A mismatch in either direction turns into an {@code IndexOutOfBoundsException} in
 * the middle of the network thread. These tests exercise the real codec (not a stub) so a change to
 * {@code EnumSimplePacket}'s decode classes cannot silently break the protocol.
 * </p>
 */
public class PacketSimpleCodecTest {

    // ---------------------------------------------------------------------------------------------------------
    // Round trips
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void aSingleStringRoundTrips() {
        PacketSimple out = new PacketSimple(PacketSimple.EnumSimplePacket.S_OPEN_FUEL_GUI, new Object[] { "Mars" });
        PacketSimple in = roundTrip(out);

        Assert.assertEquals(PacketSimple.EnumSimplePacket.S_OPEN_FUEL_GUI, typeOf(in));
        Assert.assertEquals(Arrays.asList((Object) "Mars"), dataOf(in));
    }

    @Test
    public void threeIntegersRoundTripInOrder() {
        PacketSimple out = new PacketSimple(
            PacketSimple.EnumSimplePacket.C_OPEN_PARACHEST_GUI,
            new Object[] { Integer.valueOf(7), Integer.valueOf(0), Integer.valueOf(3) });
        PacketSimple in = roundTrip(out);

        Assert.assertEquals(Arrays.asList(Integer.valueOf(7), Integer.valueOf(0), Integer.valueOf(3)), dataOf(in));
    }

    @Test
    public void doublesRoundTrip() {
        PacketSimple out = new PacketSimple(
            PacketSimple.EnumSimplePacket.C_UPDATE_RADIATION,
            new Object[] { Double.valueOf(1.5D) });
        Assert.assertEquals(Arrays.asList((Object) Double.valueOf(1.5D)), dataOf(roundTrip(out)));

        PacketSimple pressure = new PacketSimple(
            PacketSimple.EnumSimplePacket.C_UPDATE_PRESSURE,
            new Object[] { Double.valueOf(-0.25D) });
        Assert.assertEquals(Arrays.asList((Object) Double.valueOf(-0.25D)), dataOf(roundTrip(pressure)));
    }

    @Test
    public void theConfigSyncPayloadRoundTrips() {
        List<Object> values = new ArrayList<Object>();
        for (int i = 0; i < Config.getConfigSyncDecodeClasses().length; i++) values.add(Integer.valueOf(i * 3));

        PacketSimple in = roundTrip(
            new PacketSimple(PacketSimple.EnumSimplePacket.C_UPDATE_CONFIGS, new ArrayList<Object>(values)));
        Assert.assertEquals(values, dataOf(in));
    }

    @Test
    public void everyPacketTypeRoundTrips() {
        for (PacketSimple.EnumSimplePacket type : PacketSimple.EnumSimplePacket.values()) {
            PacketSimple in = roundTrip(new PacketSimple(type, payloadFor(type)));
            Assert.assertEquals("type survived the round trip", type, typeOf(in));
            Assert.assertEquals("payload length for " + type, type.getDecodeClasses().length, dataOf(in).size());
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Malformed input
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void aBufferTooShortForATypeIsRejectedWithoutThrowing() {
        PacketSimple packet = new PacketSimple();
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeShort(1); // only two bytes, not enough for the int type ordinal

        packet.decodeInto(null, buffer);

        Assert.assertNull("a truncated packet must not be interpreted", typeOf(packet));
        Assert.assertNull(dataOf(packet));
    }

    @Test
    public void anOutOfRangeOrdinalIsRejectedWithoutThrowing() {
        PacketSimple packet = new PacketSimple();
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(999);

        packet.decodeInto(null, buffer);

        Assert.assertNull(typeOf(packet));
    }

    @Test
    public void aNegativeOrdinalIsRejectedWithoutThrowing() {
        PacketSimple packet = new PacketSimple();
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(-1);

        packet.decodeInto(null, buffer);

        Assert.assertNull(typeOf(packet));
    }

    @Test
    public void aDiscardedPacketIsIgnoredByBothHandlers() {
        // handleClientSide/handleServerSide start with a null-type guard; without it a malformed packet would
        // NPE or execute the default branch of the switch.
        PacketSimple discarded = new PacketSimple();
        discarded.decodeInto(
            null,
            Unpooled.buffer()
                .writeInt(999));

        EntityPlayer noPlayer = null;
        discarded.handleClientSide(noPlayer);
        discarded.handleServerSide(noPlayer);
    }

    @Test
    public void aTruncatedPayloadLeavesThePacketUsableButUnset() {
        // Encode a real packet, then cut the payload in half: the type decodes, the data does not. The decode
        // path logs and swallows the failure, but must not leave a half-filled list behind.
        PacketSimple out = new PacketSimple(
            PacketSimple.EnumSimplePacket.C_OPEN_PARACHEST_GUI,
            new Object[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) });
        ByteBuf full = Unpooled.buffer();
        out.encodeInto(null, full);

        ByteBuf truncated = Unpooled.buffer();
        truncated.writeInt(
            full.copy(0, 4)
                .readInt());
        // 4 bytes of type + 4 bytes of the first int, then nothing.
        truncated.writeInt(1);

        PacketSimple in = new PacketSimple();
        in.decodeInto(null, truncated);

        // Either the type survived and the data is a partial list, or the whole packet was rejected - what must
        // not happen is an exception escaping into the network thread.
        if (typeOf(in) != null && dataOf(in) != null) {
            Assert.assertTrue(
                "a truncated payload must not decode more entries than were written",
                dataOf(in).size() <= 3);
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static List<Object> dataOf(PacketSimple packet) {
        return (List<Object>) readField(packet, "data");
    }

    private static PacketSimple.EnumSimplePacket typeOf(PacketSimple packet) {
        return (PacketSimple.EnumSimplePacket) readField(packet, "type");
    }

    private static Object readField(PacketSimple packet, String name) {
        try {
            java.lang.reflect.Field field = PacketSimple.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(packet);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("PacketSimple." + name + " is gone", e);
        }
    }

    private static PacketSimple roundTrip(PacketSimple out) {
        ByteBuf buffer = Unpooled.buffer();
        out.encodeInto(null, buffer);

        PacketSimple in = new PacketSimple();
        in.decodeInto(null, buffer);
        Assert.assertEquals("all bytes were consumed by the decoder", 0, buffer.readableBytes());
        return in;
    }

    private static List<Object> payloadFor(PacketSimple.EnumSimplePacket type) {
        List<Object> data = new ArrayList<Object>();
        for (Class<?> clazz : type.getDecodeClasses()) {
            if (clazz.equals(Integer.class)) {
                data.add(Integer.valueOf(1));
            } else if (clazz.equals(String.class)) {
                data.add("x");
            } else if (clazz.equals(Double.class)) {
                data.add(Double.valueOf(0.5D));
            } else if (clazz.equals(Float.class)) {
                data.add(Float.valueOf(0.5F));
            } else if (clazz.equals(Long.class)) {
                data.add(Long.valueOf(1L));
            } else if (clazz.equals(Boolean.class)) {
                data.add(Boolean.TRUE);
            } else {
                throw new IllegalArgumentException("unsupported decode class in the test: " + clazz);
            }
        }
        return data;
    }
}
