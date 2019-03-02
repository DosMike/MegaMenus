package de.dosmike.sponge.megamenus.impl;

import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;

import java.util.LinkedList;
import java.util.List;

public class ChatRenderer extends TextMenuRenderer {

    int pageHeight;

    @SuppressWarnings("deprecation")
    @Deprecated
    public ChatRenderer(IMenu menu, int pageHeight) {
        super(menu);
        this.pageHeight = pageHeight;
    }

    @Override
    void render(Player viewer) {
        PaginationList.Builder builder = PaginationList.builder();
        builder.title(menu.getTitle());
        builder.linesPerPage(pageHeight+2);

        List<Text> contents = new LinkedList<>();
        for (int i=1; i<=getMenu().pages(); i++) {
            int elementsInPage = 0;
            List<IElement> elements = new LinkedList<>(menu.getPageElements(i));
            elements.sort((e1, e2) -> {
                SlotPos p1 = e1.getPosition(), p2 = e2.getPosition();
                int c = Integer.compare(p1.getY(), p2.getY());
                if (c == 0) c = Integer.compare(p1.getX(), p2.getX());
                return c;
            });
            for(IElement e : elements) {
                if (elementsInPage++ >= pageHeight) break;
                contents.add(e.renderTUI(viewer));
            }
            while (elementsInPage++ < pageHeight)
                contents.add(Text.EMPTY);
        }
        builder.contents(contents);

        builder.sendTo(viewer);
    }

}
