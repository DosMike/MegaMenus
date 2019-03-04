package de.dosmike.sponge.megamenus.impl;

import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.Text;

import java.util.LinkedList;
import java.util.List;

/**
 * Basic implementation of Rendering the menu in a BookView
 * @see de.dosmike.sponge.megamenus.api.MenuRenderer
 */
public class BookRenderer extends TextMenuRenderer {

    @SuppressWarnings("deprecation")
    @Deprecated
    public BookRenderer(IMenu menu) {
        super(menu);
    }

    @Override
    void render(Player viewer) {
        BookView.Builder builder = BookView.builder()
                .title(menu.getTitle());

        for (int i=1; i<=menu.pages(); i++) {
            Text.Builder page = Text.builder();
            List<IElement> elements = new LinkedList<>(menu.getPageElements(i));
            elements.sort((e1, e2) -> {
                SlotPos p1 = e1.getPosition(), p2 = e2.getPosition();
                int c = Integer.compare(p1.getY(), p2.getY());
                if (c == 0) c = Integer.compare(p1.getX(), p2.getX());
                return c;
            });
            for(IElement e : elements) {
                page.append(e.renderTUI(viewer), Text.NEW_LINE);
            }
            builder.addPage(page.build());
        }

        builder.author(Text.of("MegaMenu"));
        viewer.sendBookView(builder.build());
    }

}
