package de.dosmike.sponge.megamenus.compat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.dosmike.sponge.megamenus.MegaMenus;
import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.elements.*;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.compat.events.HookButtonClickEvent;
import de.dosmike.sponge.megamenus.compat.events.HookSlotChangeEvent;
import de.dosmike.sponge.megamenus.compat.events.HookValueChangeEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.intellij.lang.annotations.MagicConstant;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class IMenuJsonBuilder {

    /** Creates a menu from a fat Json. This includes all elements on the menu.<br>
     * Json syntax:
     * <pre>
     *     {
     *         "title": "&amp;-esacped string",
     *         "page,x,y": { element definition },
     *         "page,x,y": { element definition },
     *         ...
     *     }
     * </pre>*/
    public static IMenu menuFromJson(JsonObject json) {
        IMenu menu = MegaMenus.createMenu();

        menu.setTitle(TextSerializers.FORMATTING_CODE.deserialize(json.get("title").getAsString()));
        json.entrySet().stream().filter(e->e.getKey().matches("[1-9][0-9]*,[0-9]+,[0-9]+")).forEach(entry->{
            int page;
            SlotPos pos;
            {
                String[] s = entry.getKey().split(",");
                page = Integer.parseInt(s[0]);
                pos = SlotPos.of(Integer.parseInt(s[1]), Integer.parseInt(s[2]));
            }
            JsonObject data = entry.getValue().getAsJsonObject();
            IElement element = elementFromJson(data);
            if (element != null) {
                element.setPosition(pos);
                menu.add(page, element);
            }

        });
        return menu;
    }

    /**
     * Create a subclass of {@link IElement} based on the defined type with the specified properties.
     * The position is not set during building. <br>
     * Json Syntax:
     * <pre>
     * {
     *   "type": "icon|button|checkbox|spinner|slot",   # only one
     *   "icon": &lt; icon definition &gt;           # for type button|icon
     *   "name": "&amp;-escaped string"            # not for type slot
     *   "lore": "&amp;-escaped string"            # not for type slot
     *   "values": [ &lt; icon definition &gt; ]     # for type spinner
     *   "access": "put|take"                  # for type slot; either "", "put", "take", or "put|take"
     * }
     * </pre>
     * <table>
     * <caption>Elements are binding different sponge events to element action listeners where applicable:</caption>
     * <tr><td>icon</td><td>-</td></tr>
     * <tr><td>button</td><td>{@link HookButtonClickEvent}</td></tr>
     * <tr><td>checkbox</td><td>{@link HookValueChangeEvent}&lt;Integer&gt;</td></tr>
     * <tr><td>spinner</td><td>{@link HookValueChangeEvent}&lt;Integer&gt;</td></tr>
     * <tr><td>slot</td><td>{@link HookSlotChangeEvent}</td></tr>
     * </table>
     *
     * @return the IElement from the {@link JsonObject} if the type was valid, <i>null</i> otherwise
     */
    @Nullable
    public static IElement elementFromJson(JsonObject data) {
        String type = data.get("type").getAsString();
        Text name = TextSerializers.FORMATTING_CODE.deserialize(data.get("name").getAsString());
        List<Text> lore = StreamSupport.stream(data.get("lore").getAsJsonArray().spliterator(), false)
                .map(e->TextSerializers.FORMATTING_CODE.deserialize(e.getAsString()))
                .collect(Collectors.toList());
        IElement element = null;
        switch (type.toLowerCase()) {
            case "icon": {
                element = MIcon.builder()
                        .setName(name)
                        .setLore(lore)
                        .setIcon(iiconFromJson(data.get("icon").getAsJsonArray()))
                        .build();
                break;
            }
            case "checkbox": {
                element = MCheckbox.builder()
                        .setName(name)
                        .setLore(lore)
                        .setOnChangeListener((o,n,e,v)->{
                            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                                Sponge.getEventManager().post(new HookValueChangeEvent<>(o, n, (MCheckbox) e, v));
                            }
                        })
                        .build();
                break;
            }
            case "spinner": {
                MSpinner.Builder builder = MSpinner.builder()
                        .setName(name);
                for (Map.Entry<String, JsonElement> entry : data.get("values").getAsJsonObject().entrySet()) {
                    builder.addValue(iiconFromJson(entry.getValue()), TextSerializers.FORMATTING_CODE.deserialize(entry.getKey()) );
                }
                builder.setOnChangeListener((o,n,e,v)->{
                    try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                        Sponge.getEventManager().post(new HookValueChangeEvent<>(o, n, (MSpinner) e, v));
                    }
                });
                element = builder.build();
                break;
            }
            case "button": {
                String commandCallback = data.get("command").getAsString();
                element = MButton.builder()
                        .setName(name)
                        .setLore(lore)
                        .setIcon(iiconFromJson(data.get("icon").getAsJsonArray()))
                        .setOnClickListener((e,v,b,s)->{
                            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                                Sponge.getEventManager().post(new HookButtonClickEvent((MButton) e, v, b, s));
                            }
                        })
                        .build();
                break;
            }
            case "slot": {
                element = MSlot.builder()
                        .setAccess(accessRulesFromString(data.get("access").getAsString()))
                        .build();
                ((MSlot) element).setSlotChangeListener((c,e,v)->{
                    try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                        Sponge.getEventManager().post(new HookSlotChangeEvent((MSlot) e, c, v));
                    }
                });
                break;
            }
        }
        return element;
    }

    @MagicConstant(intValues = {0,1,2,3})
    private static int accessRulesFromString(String value) {
        int v = 0;
        String[] rules = value.toLowerCase().split("\\|");
        if (ArrayUtils.contains(rules, "put"))
            v |= IElement.GUI_ACCESS_PUT;
        if (ArrayUtils.contains(rules, "take"))
            v |= IElement.GUI_ACCESS_TAKE;
        return v;
    }
    private static final Pattern p = Pattern.compile("<((?:[a-z]*:)?[a-z]+)(?::([0-9]+))?>(\\{[1-9][0-9]*})?");
    /** accepts either a string (see itemStackFromZenHandler) or a objkect that containes fps and items[] */
    private static IIcon iiconFromJson(JsonElement element) {
        if (element.isJsonPrimitive()) {
            ItemStack stack = itemStackFromZenHandler(element.getAsString());
            return stack == null ? null : IIcon.of(stack);
        } else {
            return IIcon.builder()
                    .addFrameItemStacks(
                            StreamSupport.stream(element.getAsJsonObject().get("items").getAsJsonArray().spliterator(), false)
                                    .map(e -> itemStackFromZenHandler(e.getAsString()))
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList()))
                    .setFPS(Math.min(element.getAsJsonObject().get("fps").getAsDouble(), 20.0))
                    .build();
        }
    }
    /** accept a modified zen bracket handler (see pattern P) that builds a purely visual itemstack */
    private static ItemStack itemStackFromZenHandler(String itemType) {
        Matcher m = p.matcher(itemType);
        if (m.matches()) {
            ItemType type = Sponge.getRegistry().getType(ItemType.class, m.group(1)).orElse(ItemTypes.AIR);
            int meta = (m.group(2) != null)?Integer.parseInt(m.group(2)):0;
            int amount = (m.group(3) != null)?Integer.parseInt(m.group(3)):1;
            return ItemStack.builder().fromContainer(ItemStack.of(type, amount).toContainer().set(DataQuery.of("UnsafeDamage"), meta)).build();
        } else return null;
    }

}
