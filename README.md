# Mega Menus

This menu API focuses on a fluent experience with redrawing inventories that **do 
not reset the cursor on every click**. A inventory menu with this API keeps the 
**player inventory unlocked** and thus allowes for the inventory menu to accept and 
output items in a controlled manner.

The API itself was influenced by java AWT/Swing but is Builder oriented.
Additionally the API will **automatically generate pagination** for menus with 
elements put on different pages.

Supported menu elements are:
* Icon - does nothing, but looks pretty
* Button - has a click callback
* Checkbox - can be toggled on and off, has a change callback, code can set tri-state
* Spinner - add a list of values, has a change callback
* Slot - can provide or accept single stacks of items (not shift-clickable), has a slot chagne callback

Additionally menus hold **state objects** for the menu and every player in the menu.
These objects do not reset automatically and are persistent until the menu is 
GCed. Menus can be rendered as shared, potentially allowing multiple player to 
interact on the same instance, or create a bound instance, that provides a 
separate instance for all elements so interaction does not clash between 
viewers.

All Elements on the menu render with Icons, collections of ItemStacks that 
animates with a given FPS or frametime.

My **Minesweeper plugin** is a pretty good demo plugin to get things started, or
take a look at this (old) example:
<pre>
static BaseMenuImpl menu = (BaseMenuImpl)MegaMenus.createMenu();
static MSlot outSlot = new MSlot(ItemStack.empty());
static {
  menu.setTitle(Text.of("This is a test"));
  menu.setBackgroundProvider(BackgroundProvider.BACKGROUND_GRAYPANE);
  // example for spinner
  menu.add(MSpinner.builder()
    .addValue(IIcon.of(ItemStack.builder()
      .itemType(ItemTypes.DYE)
      .add(Keys.DYE_COLOR, DyeColors.RED)
      .build()))
    .addValue(IIcon.of(ItemStack.builder()
      .itemType(ItemTypes.DYE)
      .add(Keys.DYE_COLOR, DyeColors.ORANGE)
      .build()))
    .addValue(IIcon.of(ItemStack.builder()
      .itemType(ItemTypes.DYE)
      .add(Keys.DYE_COLOR, DyeColors.YELLOW)
      .build()))
    .addValue(IIcon.of(ItemStack.builder()
      .itemType(ItemTypes.DYE)
      .add(Keys.DYE_COLOR, DyeColors.GREEN)
      .build()))
    .setName(Text.of("Pick a Color"))
    .setLore(Arrays.asList(
      Text.of("Red"),
      Text.of("Orange"),
      Text.of("Yellow"),
      Text.of("Green")
    ))
    .setPosition(new SlotPos(0, 0))
    .build());
  // example for checkbox with callback
  menu.add(2, MCheckbox.builder()
    .setName(Text.of("Vanish"))
    .setLore(Arrays.asList(
      Text.of("If you're in vanish other"),
      Text.of("players won't see you")
      ))
    .setPosition(new SlotPos(4,1))
    .setOnChangeListener((o,n,e,v)->{
      v.offer(Keys.VANISH, n==1);
      if (n==1)
        v.sendMessage(Text.of("You're now in vanish"));
      else
        v.sendMessage(Text.of("You've left vanish"));
    })
    .setValue(0)
    .build());

  // example for complex slot behaviour
  // hooking think tick to implement a "machine", converting items into coal over time
  // (progress get's lost if inventory is closed in this example because states were not used)
  MSlot inSlot = new MSlot(ItemStack.empty());
  inSlot.hookThinkTick(new Tickable() {
    int time=0;
    @Override
    public boolean tick(int ms) {
      if (inSlot.getItemStack().isPresent() && outSlot.getItemStack().map(ItemStack::getQuantity).orElse(0) < 64) {
        time += ms;
        if (time > 1000) {
          time -= 1000;
          ItemStack stack = inSlot.getItemStack().get();
          stack.setQuantity(stack.getQuantity() - 1);
          if (!outSlot.getItemStack().isPresent())
            outSlot.setItemStack(ItemTypes.COAL);
          else {
            stack = outSlot.getItemStack().get();
            stack.setQuantity(stack.getQuantity() + 1);
          }
          return true;
        }
      } else {
        time = 0;
      }
      return false;
    }
  });
  inSlot.setPosition(new SlotPos(3,1));
  inSlot.setAccess(IElement.GUI_ACCESS_PUT);
  outSlot.setPosition(new SlotPos(5,1));
  outSlot.setAccess(IElement.GUI_ACCESS_TAKE);
  menu.add(3, inSlot);
  menu.add(3, outSlot);
  menu.add(3, MIcon.builder()
    .setIcon(IIcon.of(ItemStack.builder().itemType(ItemTypes.COAL).build()))
    .setName(Text.of(TextColors.Reset, "What's this?"))
    .setLore(Collections.singletonList(Text.of("Converts any item into coal")))
    .setPosition(new SlotPos(4,0))
    .build());
}
...
// open the menu with instance bound element values
menu.createBoundGuiRenderer(3).open(player);
</pre>