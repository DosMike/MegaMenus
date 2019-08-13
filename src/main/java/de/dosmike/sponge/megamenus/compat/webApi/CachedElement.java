package de.dosmike.sponge.megamenus.compat.webApi;

import de.dosmike.sponge.megamenus.api.elements.*;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.compat.events.ContextKeys;
import de.dosmike.sponge.megamenus.compat.events.MenuButtonClickEvent;
import de.dosmike.sponge.megamenus.compat.events.MenuSlotChangeEvent;
import de.dosmike.sponge.megamenus.compat.events.MenuValueChangeEvent;
import de.dosmike.sponge.megamenus.impl.elements.IElementImpl;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import valandur.webapi.cache.CachedObject;
import valandur.webapi.exceptions.NotImplementedException;
import valandur.webapi.serialize.JsonDetails;
import valandur.webapi.shadow.com.fasterxml.jackson.annotation.JsonIgnore;
import valandur.webapi.shadow.io.swagger.annotations.ApiModel;
import valandur.webapi.shadow.io.swagger.annotations.ApiModelProperty;
import valandur.webapi.shadow.javax.ws.rs.BadRequestException;
import valandur.webapi.util.Constants;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/** WebAPI cached IElement */
@ApiModel("MegaMenusElement")
public class CachedElement extends CachedObject<IElement> {

    private ItemStackSnapshot icon;
    private String name;
    private List<String> lore;
    private String type;
    @JsonIgnore
    private int page, x, y;
    private Integer access;
    private UUID menuId;

    public CachedElement() {
        super(null);
    }

    public CachedElement(IElement element, int page) {
        super(element);
        if (!((element instanceof MButton) ||
                (element instanceof MCheckbox) ||
                (element instanceof MIcon) ||
                (element instanceof MLabel) ||
                (element instanceof MSlot) ||
                (element instanceof MSpinner)
                ))
            throw new IllegalArgumentException("Element of type "+element.getClass().getSimpleName()+" not supported");
        //standard elements are all sufficiently described with icon, name, lore and type
        this.type       = element.getClass().getSimpleName().substring(1).toLowerCase();
        //standard elements ignore the viewer and this can accept null, maybe bad otherwise
        this.icon       = element.getIcon(null).render(); //IIcon only exists as interface, can't extract frames that way
        Text tName      = element.getName(null);
        this.name       = tName==null?null:TextSerializers.FORMATTING_CODE.serialize(tName);
        List<Text> tLore = element.getLore(null);
        this.lore       = tLore==null?null:tLore.stream().map(TextSerializers.FORMATTING_CODE::serialize).collect(Collectors.toList());
        this.x          = element.getPosition().getX();
        this.y          = element.getPosition().getY();
        this.access     = element.getAccess();

        this.page       = page;
        this.menuId     = element.getParent().getUniqueId();
    }

    @ApiModelProperty(value = "Icon for this element",
            notes = "Due to the nature of IIcons, WebAPI can only support static icons")
    @JsonDetails
    public ItemStackSnapshot getIcon() {
        return icon;
    }

    @ApiModelProperty(value = "The name of this element, displayed as item name")
    @JsonDetails
    public String getName() {
        return name;
    }

    @ApiModelProperty(value = "The lore or values of this element, displayed in the item tooltip as lore")
    @JsonDetails
    public List<String> getLore() {
        return lore;
    }

    @ApiModelProperty(value = "The type of this element",
            notes = "Only the standard elements are supported, as they behave in a consistent way that can always be described with the properties icon, name and lore")
    @JsonDetails
    public String getType() {
        return type;
    }

    @ApiModelProperty(value = "The Column of this element within the inventory [0..8]",
            hidden = true)
    public int getX() {
        return x;
    }

    @ApiModelProperty(value = "The Row of this element within the inventory [0..5]",
            notes = "May not exceed the renderer's height",
            hidden = true)
    public int getY() {
        return y;
    }

    @ApiModelProperty(value = "The page this element was added to",
            hidden = true)
    public int getPage() {
        return page;
    }

    @ApiModelProperty(value = "Access properties applicable to MSlot type elements",
            notes = "0 = locked, 1 = can take, 2 = can put, 3 = both")
    @JsonDetails
    public Integer getAccess() {
        return access;
    }

    @Override
    public Optional<IElement> getLive() {
        throw new NotImplementedException();
    }

    @Override
    public String getLink() {
        return String.format("%s/megamenus/menu/%s/%d/%d/%d",
                Constants.BASE_PATH,
                menuId.toString(),
                page,
                y,
                x
                );
    }

    /** creates a new element instance for the stored configuration in this cached element and returns it */
    public IElement createInstance() throws BadRequestException {
        IElement ielement;
        if (getType() == null) throw new BadRequestException("Missing element `type`: { button, checkbox, icon, slot }");
        switch (getType().toLowerCase()) {
            case "button": {
                MButton.Builder builder = MButton.builder();
                if (getIcon() == null)
                    throw new BadRequestException("Buttons require at least `icon`: ItemStack");
                builder.setIcon(IIcon.of(getIcon()));
                if (getName()!=null)
                    builder.setName(TextSerializers.FORMATTING_CODE.deserialize(getName()));
                if (getLore()!=null)
                    builder.setLore(getLore().stream().map(TextSerializers.FORMATTING_CODE::deserialize).collect(Collectors.toList()));
                if (getAccess()!=null)
                    throw new BadRequestException("Icons can't take any `access`: Integer");
                builder.setOnClickListener((element, player, button, shift) -> {
                    try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                        IElementImpl elem = (IElementImpl) element;
                        frame.addContext(ContextKeys.menuID, elem.getParent().getUniqueId());
                        frame.addContext(ContextKeys.menuColumn, elem.getPosition().getX());
                        frame.addContext(ContextKeys.menuRow, elem.getPosition().getY());
                        frame.addContext(ContextKeys.mouseButton, button);
                        frame.addContext(ContextKeys.shiftHeld, shift);
                        Sponge.getEventManager().post(new MenuButtonClickEvent((MButton) element, player, button, shift, frame.getCurrentCause()));
                    }
                });
                ielement = builder.build();
                break;
            }
            case "checkbox": {
                MCheckbox.Builder builder = MCheckbox.builder();
                if (getIcon() != null)
                    throw new BadRequestException("Checkbox can't take any `icon`: ItemStack");
                if (getName()!=null)
                    builder.setName(TextSerializers.FORMATTING_CODE.deserialize(getName()));
                if (getLore()!=null)
                    builder.setLore(getLore().stream().map(TextSerializers.FORMATTING_CODE::deserialize).collect(Collectors.toList()));
                if (getAccess()!=null)
                    throw new BadRequestException("Icons can't take any `access`: Integer");
                builder.setOnChangeListener((oldValue, newValue, element, viewer) -> {
                    try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                        IElementImpl elem = (IElementImpl) element;
                        frame.addContext(ContextKeys.menuID, elem.getParent().getUniqueId());
                        frame.addContext(ContextKeys.menuColumn, elem.getPosition().getX());
                        frame.addContext(ContextKeys.menuRow, elem.getPosition().getY());
                        frame.addContext(ContextKeys.targetValue, newValue);
                        Sponge.getEventManager().post(new MenuValueChangeEvent<>(oldValue, newValue, (MCheckbox) element, viewer, frame.getCurrentCause()));
                    }
                });
                ielement = builder.build();
                break;
            }
            case "icon": {
                MIcon.Builder builder = MIcon.builder();
                if (getIcon() == null)
                    throw new BadRequestException("Icons require at least `icon`: ItemStack");
                builder.setIcon(IIcon.of(getIcon()));
                if (getName()!=null)
                    builder.setName(TextSerializers.FORMATTING_CODE.deserialize(getName()));
                if (getLore()!=null)
                    builder.setLore(getLore().stream().map(TextSerializers.FORMATTING_CODE::deserialize).collect(Collectors.toList()));
                if (getAccess()!=null)
                    throw new BadRequestException("Icons can't take any `access`: Integer");
                ielement = builder.build();
                break;
            }
            case "slot": {
                MSlot.Builder builder = MSlot.builder();
                if (getIcon() == null)
                    throw new BadRequestException("Slots require at least `icon`: ItemStack");
                builder.setItemStack(getIcon().createStack());
                if (getName()!=null)
                    throw new BadRequestException("Slots can't take any `name`: String");
                if (getLore()!=null)
                    throw new BadRequestException("Slots can't take any `lore`: String[]");
                if (getAccess()!=null) {
                    int val = getAccess();
                    if ((val & ~0x03) != 0)
                        throw new BadRequestException("Invalid value for `access`: { 0, 1, 2, 3 }; 1=take, 2=put, 3=both");
                    builder.setAccess(getAccess());
                }
                builder.setOnSlotChangeListener((change, element, viewer) -> {
                    try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                        IElementImpl elem = (IElementImpl)element;
                        frame.addContext(ContextKeys.menuID, elem.getParent().getUniqueId());
                        frame.addContext(ContextKeys.menuColumn, elem.getPosition().getX());
                        frame.addContext(ContextKeys.menuRow, elem.getPosition().getY());
                        frame.addContext(ContextKeys.stackTaken, change.getItemsTaken().orElse(ItemStackSnapshot.NONE));
                        frame.addContext(ContextKeys.stackInsert, change.getItemsGiven().orElse(ItemStackSnapshot.NONE));
                        Sponge.getEventManager().post(new MenuSlotChangeEvent((MSlot) element, change, viewer, frame.getCurrentCause()));
                    }
                });
                ielement = builder.build();
                break;
            }
            default: {
                throw new BadRequestException("Invalid element type: " + getType());
            }
        }
        return ielement;
    }

}
