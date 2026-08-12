# Implementation Plan: Tier 1 Space Suit (1.12.2 → 1.7.10)

## Status

- **Scope decision (agreed):** Plain `ItemArmor` — electric system (`ElectricArmorBase`) deferred.
- **Scope decision (agreed):** Gravity boots variant deferred (4 pieces only: helmet, chest, leggings, boots).
- **Scope decision (agreed):** `IModularArmor` dropped entirely (nothing in 1.7.10 GC invokes `renderHelmetOverlay`).

### Known rendering issues (diagnosed, fix deferred)

- **Textured parts render "fuzzy/noisy" (tanks, pipes, arm piston rods).** Root cause: the original
  `space_suit.obj` has **zero UV texture coordinates** (`vt`) — all 5580 faces are `v//vn`; it is the
  only OBJ in the mod with no UVs (the 1.12.2 source asset is identical). The 1.7.10
  `WavefrontObject` leaves GL texture-coordinate state undefined for UV-less faces, so those parts
  sample their `blank_rocket_*` textures at uncontrolled coords → shimmering "fuzz" that shifts with
  camera angle (mipmap LOD). Flat `blank_rocket_white` parts hide the error, which is why they look
  clean. **Fix options (not yet applied):** render the affected parts as untextured flat colors
  (disable `GL_TEXTURE_2D` + `glColor3f`), or re-export the model with a UV unwrap. If flat-coloring
  a specific part does NOT remove its fuzz, that part is additionally Z-fighting (overlapping
  coplanar faces) and needs a polygon offset.
- **Helmet glass (HelmetPart3, additive-blend visor) has artifacts.** Same no-UV sampling issue +
  the `GL_ONE, GL_ONE` additive pass; deferred alongside the above.

## Executive Summary

The Tier 1 Space Suit from Extra Planets 1.12.2 is a space suit with a custom OBJ-based 3D
model. Porting it to 1.7.10 requires adapting the model rendering system (from Forge's 1.12.2
baked OBJ models to 1.7.10's `IModelCustom`/`WavefrontObject`), replacing missing dependencies
(`mjrlegendslib`, `ISensorGlassesArmor`, `IArmorCorrosionResistant`), and carefully preserving
every GL transformation to avoid Z-fighting.

For the first pass the suit is a plain `ItemArmor` (no energy storage). The electric system is
decoupled from rendering, so it can be layered on later by changing the parent class and
re-adding the power tooltip lines — no rendering or gameplay-behaviour changes needed.

---

## 1. Dependencies We Don't Have (and how to handle each)

### 1a. Galacticraft API interfaces — verified via `javap` on the GC 3.4.15-GTNH jar

| Interface | 1.7.10 Available? | Action |
|---|---|---|
| `IArmorGravity` | Yes — `gravityOverrideIfLow(EntityPlayer)`, `gravityOverrideIfHigh(EntityPlayer)` | Use directly |
| `IBreathableArmor` | Yes — `handleGearType(EnumGearType)`, `canBreathe(ItemStack, EntityPlayer, EnumGearType)` | Use, but `EnumGearType` is **nested**: `IBreathableArmor.EnumGearType` (not standalone like 1.12.2) |
| `IItemElectric` / `IItemElectricBase` | Yes | **Not used this pass** (electric deferred) |
| `ISensorGlassesArmor` | NOT in 1.7.10 GC | Drop — no helmet overlay interface exists |
| `IArmorCorrosionResistant` | NOT in 1.7.10 GC | Drop — marker interface for acid resistance, not available |

### 1b. `mjrlegendslib` utilities — NOT available, replace with vanilla

- `MCUtilities.getMinecraft()` → `Minecraft.getMinecraft()` (field is `thePlayer`, not `player`)
- `MCUtilities.getMinecraft().renderEngine.bindTexture(...)` → `Minecraft.getMinecraft().getTextureManager().bindTexture(...)`
- `ModelUtilities.drawBakedModel(model)` → Not needed — 1.7.10 uses `IModelCustom.renderPart("groupName")` / `renderOnly("group1", "group2", ...)` instead of baked models
- `TranslateUtilities.translate(key)` → `StatCollector.translateToLocal(key)` (vanilla 1.7.10)

### 1c. `Module` / `ModuleHelper` (modular armor system) — NOT available

- The 1.12.2 `addInformation` lists installed modules; `renderHelmetOverlay` delegates to modules.
- **Action:** Simplify `addInformation` to show only the basic shift-tooltips (no module listing).
  `IModularArmor` is dropped entirely this pass (nothing invokes it in 1.7.10).

### 1d. IC2 API — NOT confirmed in compile classpath

- The GC jar does not contain `ic2/api/item/*` classes; no IC2 jar found in gradle cache.
- **Action:** Not relevant this pass (electric deferred). When electric is added later, add IC2 as
  `compileOnly` and use `@Optional.Interface`. The Galacticraft electric + CoFH RF + Mekanism paths
  are sufficient.

### 1e. `GlStateManager` — NOT in 1.7.10

- Replace `GlStateManager.shadeModel(GL11.GL_SMOOTH)` → `GL11.glShadeModel(GL11.GL_SMOOTH)`
- All other GL state in the armor model already uses raw `GL11.*` calls, which work identically in 1.7.10.

### 1f. API surface changes (1.12.2 → 1.7.10)

| 1.12.2 | 1.7.10 |
|---|---|
| `EntityEquipmentSlot.HEAD/CHEST/LEGS/FEET` | `int` slots: `0`=helmet, `1`=chest, `2`=legs, `3`=boots |
| `ItemArmor(ArmorMaterial, int, EntityEquipmentSlot)` | `ItemArmor(ArmorMaterial, int, int)` |
| `getArmorModel(EntityLivingBase, ItemStack, EntityEquipmentSlot, ModelBiped)` | `getArmorModel(EntityLivingBase, ItemStack, int, ModelBiped)` |
| `getArmorTexture(ItemStack, Entity, EntityEquipmentSlot, String)` | `getArmorTexture(ItemStack, Entity, int, String)` |
| `addInformation(ItemStack, World, List<String>, ITooltipFlag)` | `addInformation(ItemStack, EntityPlayer, List, boolean)` |
| `getSubItems(CreativeTabs, NonNullList<ItemStack>)` | `getSubItems(Item, CreativeTabs, List)` |
| `player.inventory.armorInventory.get(N)` (List) | `thePlayer.inventory.armorInventory[N]` (array) |
| `Minecraft.getMinecraft().player` | `Minecraft.getMinecraft().thePlayer` |
| `EntityArmorStand` | Does not exist — remove `instanceof EntityArmorStand` check |
| `EnumGearType` (standalone) | `IBreathableArmor.EnumGearType` (nested) |
| `net.minecraftforge.fml.relauncher.Side/SideOnly` | `cpw.mods.fml.relauncher.Side/SideOnly` |
| `net.minecraftforge.fml.client.FMLClientHandler` | `cpw.mods.fml.client.FMLClientHandler` |

---

## 2. Files to Create

### 2a. `src/main/java/com/mjr/extraplanets/api/item/IPressureSuit.java`

Empty marker interface — identical to 1.12.2. Identifies armor that protects against low-pressure
environments.

### 2b. `src/main/java/com/mjr/extraplanets/api/item/IRadiationSuit.java`

Interface with `int getArmorTier()` — identical to 1.12.2. Used by the radiation system to
determine protection tier.
### 2c. `src/main/java/com/mjr/extraplanets/client/model/ArmorCustomModel.java`

Abstract class extending `ModelBiped` — the GL transformation framework. The most
Z-fighting-critical file.

Port from 1.12.2 with these changes:

- **Remove** the `EntityArmorStand` check (1.12.2 lines 61-62) — doesn't exist in 1.7.10
- **Keep ALL float values exactly** — the `1.1F` scale, all translation offsets, all rotation angles
- Replace any `GlStateManager` calls with raw `GL11.*`
- The `render(Entity, float, float, float, float, float, float)` signature is identical in 1.7.10 `ModelBiped`

**Z-Fighting notes for this file:**

- `GL11.glScalef(1.1F, 1.1F, 1.1F)` (1.12.2 line 48) — THE primary anti-Z-fighting measure. Scales
  the entire armor 10% larger than the player body so armor surfaces never coincide with skin
  surfaces. Must be preserved exactly.
- Per-part `glPushMatrix/glPopMatrix` blocks isolate each body part's transformations
- Sneak adjustments (e.g. `glTranslatef(0F, -0.125F, 0F)` for head, `glTranslatef(0F, -0.20F, -0.1F)`
  for body) reposition parts when crouching to prevent clipping
- The `isChild` scaling (`1.5F/f6` etc.) handles baby-style rendering

### 2d. `src/main/java/com/mjr/extraplanets/client/model/ArmorSpaceSuitModel.java`

The OBJ-based space suit model extending `ArmorCustomModel`. The most GL-rendering-critical file.

Complete rewrite of the model loading and rendering approach:

**Model loading (1.7.10 approach):**

```java
// Instead of 1.12.2's ModelLoaderRegistry + OBJModel.bake() + OBJBakedModel fields:
private static IModelCustom spaceSuitModel;

private void updateModel() {
    if (spaceSuitModel == null) {
        spaceSuitModel = AdvancedModelLoader.loadModel(
            new ResourceLocation(Constants.ASSET_PREFIX, "models/space_suit.obj"));
    }
}
```

**Part rendering (1.7.10 approach):**

Instead of `ModelUtilities.drawBakedModel(armourModelHead)`, use:

```java
// For multi-group parts (e.g. head = 12 groups):
spaceSuitModel.renderOnly("HelmetPart1", "HelmetPart2", "HelmetPart4", ...);
// For single-group parts:
spaceSuitModel.renderPart("BodyPart2");
```

Store the group name arrays as static constants to avoid recreating them each frame.

**Texture binding:**

```java
// 1.12.2: MCUtilities.getMinecraft().renderEngine.bindTexture(new ResourceLocation(...))
// 1.7.10: Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(...))
```

**Armor inventory access:**

```java
// 1.12.2: Minecraft.getMinecraft().player.inventory.armorInventory.get(3)
// 1.7.10: Minecraft.getMinecraft().thePlayer.inventory.armorInventory[3]
// Then check: .getItem() instanceof Tier0SpaceSuitArmor (for texture selection)
```

**Slot check adaptation:**

```java
// 1.12.2: if (partType == EntityEquipmentSlot.HEAD)
// 1.7.10: if (partType == 0)  // 0 = helmet
```

**Skip the Tier0 texture check and jetpack rendering** for now (Tier0 and JetpackArmorBase don't
exist yet in 1.7.10). Always use the "white" / standard textures. Add a TODO comment noting the
Tier0 light-blue texture switch and jetpack rendering should be re-enabled when those tiers are
ported.

**`pre()` method:**

```java
// 1.12.2 uses GlStateManager.shadeModel — 1.7.10 uses GL11 directly:
GL11.glEnable(GL11.GL_BLEND);
GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
GL11.glShadeModel(GL11.GL_SMOOTH);  // was GlStateManager.shadeModel
updateModel();
```

**Part-by-part rendering with exact Z-fighting values preserved:**

| Part | Slot | Key transforms (MUST preserve exactly) |
|---|---|---|
| `partHead()` | 0 (helmet) | `glTranslatef(0F, -1.525F, -0.005F)` — the `-0.005F` Z offset prevents visor Z-fight with head |
| `partBody()` | 1 (chest) | `glTranslatef(0F, -1.50F, 0F)` |
| `partRightArm()` | 1 (chest) | `glTranslatef(-0.3525F, -1.375F, 0F)`; sneak: `glScalef(1.1F, 1F, 1.3F)` |
| `partLeftArm()` | 1 (chest) | `glTranslatef(0.3525F, -1.375F, 0F)`; sneak: `glScalef(1.1F, 1F, 1.3F)` |
| `partRightLeg()` | 2 (legs) | `glScalef(1F, 1F, 1.5F)`; `glTranslatef(-0.100F, -0.7F, -0.01F)` (normal) / `(..., 0.04F)` (sneak) |
| `partLeftLeg()` | 2 (legs) | `glScalef(1F, 1F, 1.5F)`; `glTranslatef(0.100F, -0.7F, -0.01F)` (normal) / `(..., 0.04F)` (sneak) |

**OBJ group names (from 1.12.2 `ArmorSpaceSuitModel.updateModel`), to map to `renderOnly`/`renderPart`:**

| 1.12.2 field | Groups |
|---|---|
| head | `HelmetPart1`, `HelmetPart2`, `HelmetPart4`, `HelmetPart5`, `HelmetPart6`, `HelmetPart7`, `HelmetPart8`, `HelmetPart9`, `HelmetPart10`, `HelmetPart11`, `MainPartHelmet`, `SpacerAntenna2` |
| tanks (head) | `HelmetOxygenCapsule1`, `HelmetOxygenCapsule2` |
| headGlass | `HelmetPart3` |
| pipes (body) | `BodyPart1`, `BodyPart10`, `BodyPart11`, `BodyPart12`, `BodyPart13`, `BodyPart14`, `BodyPart15`, `BodyPart16`, `BodyPart17`, `BodyPart18`, `BodyPart3`, `BodyPart4`, `BodyPart5`, `BodyPart6`, `BodyPart7`, `BodyPart8`, `BodyPart9` |
| body | `BodyPart2` |
| bodyTank1 | `NitrogenTank` |
| bodyTank2 | `OxygenTank` |
| bodyTank3 | `HydrogenTank` |
| leftArm | `LeftHandPart1` |
| rightArm | `RightHandPart4` |
| leftArmSpring | `LeftHandPart2`, `LeftHandPart3`, `LeftHandPart4` |
| rightArmSpring | `RightHandPart1`, `RightHandPart2`, `RightHandPart3` |
| leftLeg | `LeftLegPart1` |
| rightLeg | `RightLegPart3` |
| leftLegPipes | `LeftLegPart2`, `LeftLegPart3` |
| rightLegPipes | `RightLegPart2`, `RightLegPart4` |
| leftBoot | `left_boot` |
| rightBoot | `right_boot` |

**Glass visor rendering (in `partHead()`):**

```java
// After rendering the solid helmet parts:
GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);     // additive blend for glass
spaceSuitModel.renderPart("HelmetPart3");         // the glass visor group
GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);  // restore
```

The additive blend makes the glass transparent without depth-fighting against the helmet frame.
The blend func is immediately restored to standard alpha blending.

**`post()` method:**

```java
GL11.glDisable(GL11.GL_BLEND);  // restore blend state — critical to not leak blend state
```

### 2e. `src/main/java/com/mjr/extraplanets/armor/Tier1SpaceSuitArmor.java`

The item class. **Extends `ItemArmor`** (plain, no electric), implements `IPressureSuit`,
`IRadiationSuit`, `IArmorGravity`, `IBreathableArmor`.

**Constructor:**

```java
// 1.7.10: int placement (0-3), not EntityEquipmentSlot
public Tier1SpaceSuitArmor(String name, ArmorMaterial material, int placement) {
    super(material, 0, placement);
    this.setCreativeTab(ExtraPlanets.ArmorTab);  // ArmorTab, not ItemsTab
    this.name = name;
}
```

> Note: render index `0` for plain armor (the custom model handles rendering); 1.12.2 used `1` for
> the electric base's render index. Since we override `getArmorModel`, the vanilla render index is
> effectively bypassed for the 3D model. `0` is consistent with the other plain EP armor classes.

**`getArmorTexture`:** Return `Constants.TEXTURE_PREFIX + "textures/model/armor/tier1_space_suit_layer_1.png"`
(or `_layer_2` for legs). These textures already exist in 1.7.10. Used as fallback; the custom
model binds its own textures per-part.

**`getArmorModel`:** Adapt to 1.7.10 signature with `int armorSlot` instead of `EntityEquipmentSlot`.
Use a `HashMap<Integer, ArmorSpaceSuitModel>` keyed by int slot.

**`addInformation`:** Adapt to 1.7.10 signature `(ItemStack, EntityPlayer, List, boolean)`. Show
basic shift-tooltips using `StatCollector.translateToLocal`. Skip the module listing (no
ModuleHelper) and skip the power tooltip block (no electric). Use `EnumColor` from Galacticraft
(available). Use `Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)` (LWJGL, same in both versions). Use
`GameSettings.getKeyDisplayString()` with
`FMLClientHandler.instance().getClient().gameSettings.keyBindSneak.getKeyCode()` (1.7.10 FML
package: `cpw.mods.fml.client.FMLClientHandler`).

**`canBreathe` / `handleGearType`:** Use `IBreathableArmor.EnumGearType` (nested). Both return `true`.

**`gravityOverrideIfLow/High`:** Check `thePlayer.inventory.armorInventory[i]` (array, not List)
for gravity boots. Since gravity boots aren't implemented yet, these will return 0 for now.

**`getArmorTier`:** Return `1` (implements `IRadiationSuit`).

---

## 3. Files to Modify

### 3a. `src/main/java/com/mjr/extraplanets/armor/ExtraPlanets_Armor.java`

- Add `TIER_1_SPACE_SUIT_ARMOR_MATERIAL` using 1.7.10
  `EnumHelper.addArmorMaterial("Tier1_SpaceSuit_Armor", 28, new int[]{2, 6, 5, 2}, 9)` (no
  SoundEvents/toughness params in 1.7.10).

  > **Beta balance decision (2026-08):** Protection lowered from Diamond-tier `{3, 8, 6, 3}`
  > (= 20, the 1.7.10 armor cap) to **Iron-tier `{2, 6, 5, 2}` (15)** so the suit isn't
  > best-in-slot combat armor on craft. Enchantability set to Iron's `9` for the interim
  > (there is no electric charging or module system yet, so 0 enchantability has no reason).
  > Durability factor kept at `28` (between Iron's 15 and Diamond's 33).
  > When the pressure + radiation system and armor modules sweep lands, the plan is:
  > protection modules become the upgrade path (port `Module`/`ModuleHelper`,
  > `ArmorProperties`-style or `LivingHurtEvent` reduction), and this material's
  > enchantability returns to `0` alongside `ElectricArmorBase.isEnchantable() == false`.
- Add static `Item` fields: `tier1SpaceSuitHelmet`, `tier1SpaceSuitChest`,
  `tier1SpaceSuitLegings`, `tier1SpaceSuitBoots`
- In `init()`: instantiate the 4 armor pieces with appropriate names and slot indices (0, 1, 2, 3)
- In registration section: register with `GameRegistry.registerItem(item, "name")`, gated behind
  `Config.pressure || Config.radiation` (new config fields)

### 3b. `src/main/java/com/mjr/extraplanets/Config.java`

- Add `public static boolean pressure;` and `public static boolean radiation;`
- Initialize in `Config.init()` from the `CONFIG_CATEGORY_ITEMS` category, defaulting to `true`

### 3c. `src/main/resources/assets/extraplanets/lang/en_US.lang`

Add item names + tooltip keys (see Section 4d).

---

## 4. Resource Files to Copy/Create

### 4a. OBJ model files

- Copy `migrate/.../models/space_suit.obj` → `src/main/resources/assets/extraplanets/models/space_suit.obj`
- Copy `migrate/.../models/space_suit.mtl` → `src/main/resources/assets/extraplanets/models/space_suit.mtl`
- The `.mtl` references `map_Kd extraplanets:model/blank_rocket_dark_grey` — `blank_rocket_dark_grey.png`
  already exists in 1.7.10, so this resolves correctly. The 1.7.10 WavefrontObject loader reads the
  .mtl for material parsing but we override textures manually per-part.

### 4b. Missing textures (copy from 1.12.2 `textures/model/`)

- `blank_rocket_blue.png` → MISSING in 1.7.10 — used for NitrogenTank body part
- `blank_rocket_dark_red.png` → MISSING in 1.7.10 — used for OxygenTank and HelmetOxygenCapsule

Already present in 1.7.10 (verified): `blank_rocket_white.png`, `blank_rocket_textured.png`,
`blank_rocket_dark_grey.png`, `blank_rocket_blue_textured.png`, `blank_rocket_light_blue_textured.png`.

### 4c. Armor layer textures

- `tier1_space_suit_layer_1.png` and `tier1_space_suit_layer_2.png` — already exist in 1.7.10
  `textures/model/armor/`. Used as fallback `getArmorTexture`.

### 4d. Lang file entries

Add to `en_US.lang` (and other lang files as appropriate):

```
item.tier1_space_suit_helmet.name=Tier 1 Space Suit Helmet
item.tier1_space_suit_chest.name=Tier 1 Space Suit Chest
item.tier1_space_suit_legings.name=Tier 1 Space Suit Leggings
item.tier1_space_suit_boots.name=Tier 1 Space Suit Boots
```

Info tooltip keys:

```
space.suit.information=...
space.suit.information.2=...
space.suit.information.extra=...
space.suit.information.extra.2=...
space.suit.information.extra.3=...
space.suit.information.extra.4=...
item_desc.spacesuit.shift.name=...
```

(The power-info keys `space.suit.power.info*` are NOT needed this pass — they were an
`ElectricArmorBase` concern and are deferred with the electric system.)

---

## 5. GL Rendering & Z-Fighting — Detailed Analysis

### 5a. The rendering pipeline (1.7.10)

1. `RenderPlayer` calls `item.getArmorModel(entity, stack, slot, default)` → returns our `ArmorSpaceSuitModel`
2. `RenderPlayer` calls `item.getArmorTexture(stack, entity, slot, type)` → binds fallback texture
3. `RenderPlayer` calls `model.render(entity, ...)` → our `ArmorCustomModel.render()` executes
4. `render()` calls `pre()`, applies `glScalef(1.1F)`, then each `partXxx()` method
5. Each `partXxx()` checks the slot, applies its transforms, binds the appropriate texture, and
   calls `spaceSuitModel.renderOnly(...)` / `renderPart(...)`
6. `render()` calls `post()` to disable blending

### 5b. Z-Fighting prevention measures (all must be preserved exactly)

1. **Global 1.1× scale** (`ArmorCustomModel` line 48): `GL11.glScalef(1.1F, 1.1F, 1.1F)` — lifts
   all armor geometry 10% off the player's body. This is the single most important anti-Z-fighting
   value.

2. **Per-part Z-offsets**: The head's `-0.005F` Z translation, the legs' `-0.01F` (normal) vs
   `0.04F` (sneak) Z translations — these tiny offsets push parts forward/backward to avoid
   coplanar surfaces with the player model.

3. **Per-part scales**: Legs use `glScalef(1F, 1F, 1.5F)` to make the leg armor thicker in Z
   (prevents the thin leg armor from fighting with the player's legs). Sneaking arms use
   `glScalef(1.1F, 1F, 1.3F)` for similar reasons.

4. **Blend state for glass visor**: The glass (`HelmetPart3`) is rendered with additive blending
   (`GL_ONE, GL_ONE`) which doesn't compete in the depth buffer the same way, preventing Z-fight
   between the glass and the helmet frame. The blend func is restored immediately after.

5. **Sneak body adjustments**: `glTranslatef(0F, -0.20F, -0.1F)` for the body and
   `glTranslatef(0F, -0.225F, -0.10F)` for legs reposition armor when the player crouches,
   preventing the armor from clipping into the bent player model.

6. **Push/pop matrix isolation**: Each body part is wrapped in `glPushMatrix`/`glPopMatrix` so
   transformations don't leak between parts (which could cause cumulative offset errors that
   manifest as Z-fighting).

### 5c. Potential 1.7.10-specific GL concerns

- **V-coordinate flipping**: The 1.12.2 model is processed with `flip-v: true`. The 1.7.10
  WavefrontObject loader does NOT have this option. If textures appear upside-down on the suit,
  we'll need to either: (a) edit the OBJ to flip V coords, or (b) apply a GL texture matrix flip.
  **This must be tested visually.**
- **Depth buffer**: 1.7.10 and 1.12.2 both use 24-bit depth by default — no difference expected.
- **Blend state leakage**: `pre()` enables blend, `post()` disables it. If an exception occurs
  between them, blend could leak. This is the same risk as 1.12.2. The `glPushMatrix`/`glPopMatrix`
  in `render()` protects the matrix stack but not the blend state. This is acceptable (same as
  1.12.2 behavior).

---

## 6. Implementation Order

1. Copy resources: `space_suit.obj`, `space_suit.mtl`, `blank_rocket_blue.png`, `blank_rocket_dark_red.png`
2. Create API interfaces: `IPressureSuit`, `IRadiationSuit`
3. Create `ArmorCustomModel`: port GL framework, remove EntityArmorStand check, replace GlStateManager
4. Create `ArmorSpaceSuitModel`: rewrite for IModelCustom + renderOnly/renderPart, adapt slot
   checks to int, adapt texture binding, preserve all transforms
5. Create `Tier1SpaceSuitArmor`: port item class with 1.7.10 signatures (plain `ItemArmor`)
6. Modify `ExtraPlanets_Armor`: add material, fields, instantiation, registration
7. Modify `Config`: add `pressure` and `radiation` flags
8. Add lang entries to `en_US.lang`
9. Build & test: `./gradlew build` then `./gradlew runClient` to visually verify the suit renders
   without Z-fighting and textures aren't flipped

---

## 7. Deferred Items (revisit later)

| Item | Deferred because | Re-enable trigger |
|---|---|---|
| `ElectricArmorBase` (energy storage) | Agreed to simplify first pass | Layer on by changing parent class + re-adding power tooltips; no rendering changes |
| Gravity boots variant (5th piece) | Agreed to defer | Add `tier1SpaceSuitGravityBoots` + `gravityOverrideIfLow/High` logic |
| `IModularArmor` interface + modules | Nothing in 1.7.10 GC invokes `renderHelmetOverlay` | Re-add when sensor-glasses/helmet-overlay system is ported |
| IC2 compat (`IElectricItem`/`ISpecialElectricItem`) | IC2 API not in compile classpath | Add IC2 as `compileOnly` + `@Optional.Interface` when needed |
| Tier0 light-blue texture switch in model | Tier0 doesn't exist in 1.7.10 yet | Re-enable the `instanceof Tier0SpaceSuitArmor` checks when Tier0 is ported |
| Jetpack rendering in `partBody()` | `JetpackArmorBase` doesn't exist in 1.7.10 yet | Re-enable the jetpack render block when jetpack variants are ported |

---

## 8. Open Questions / Risks

1. **OBJ V-flip**: The 1.7.10 WavefrontObject loader may render textures upside-down compared to
   1.12.2's `flip-v: true` processing. Needs visual testing. Mitigation: edit OBJ V coordinates or
   apply GL texture matrix flip.

2. **OBJ group name matching**: The 1.7.10 loader must find all group names (e.g. "HelmetPart1",
   "BodyPart2", "NitrogenTank", "left_boot", etc.) in the OBJ file. The 1.12.2 code references
   these exact names, so they should be present as `g` (group) statements in the .obj. Needs
   verification that the 1.7.10 parser reads them the same way.

3. **`space_suit.mtl` texture path**: The .mtl references `extraplanets:model/blank_rocket_dark_grey`.
   The 1.7.10 loader may interpret this as a ResourceLocation. If it fails, we can edit the .mtl to
   use a relative path or a path that the 1.7.10 loader accepts. Since we bind textures manually
   per-part, the .mtl texture is only used during initial OBJ parsing (material assignment), not
   during our custom rendering.

