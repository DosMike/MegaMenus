# Mega Menus

This menu API focuses on a fluent experience with redrawing inventories that **do 
not reset the cursor on every click**. A inventory menu with this API keeps the 
**player inventory unlocked** and thus allowes for the inventory menu to accept and 
output items in a controlled manner.

# Command for Server Admins

If a player triggered the anti glitch system you can pardon them with  
`/megamenus pardon <PLAYER>` (permission `megamenus.command.pardon`)  
If you changed bits in the config you can reload it with  
`/megamenus reload` (permission `megamenus.command.reload`)

If you don't feel like typing megamenus, `/mm` is a command alias.

# Boring dev stuff

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

My **Minesweeper plugin** is a pretty good demo plugin to get things started

## Depending on this plugin

This plugin is jitpack-compatible, if you're using gradle just add this:
```{groovy}
repositories {
    ...
    maven { url "https://jitpack.io" }
}
dependencies {
    ...
    compile 'com.github.DosMike:MegaMenus:master-SNAPSHOT'
}
```

## External Connections

**[Version Checker](https://github.com/DosMike/SpongePluginVersionChecker)**  
This plugin uses a version checker to notify you about available updates.  
This updater is **disabled by default** and can be enabled in `config/megamenus.conf`
by setting the value `VersionChecker` to `true`.    
If enabled it will asynchronously check (once per server start) if the Ore repository has any updates.  
This will *only print update notes into the server log*, no files are being downlaoded!