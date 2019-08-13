package de.dosmike.sponge.megamenus.api.elements;

import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import org.spongepowered.api.item.inventory.property.SlotPos;

/**
 * These providers help automatically determining the next {@link SlotPos} a element should
 * be placed in a {@link IMenu} based on the previous slot a {@link IElement} was added to.
 * When building a default menu element ({@link MButton}, ...) the position will be initialized
 * to the provided next slot position, if a PositionProvider was set on the menu, where the
 * {@code previous} argument passed to the provider is the position of the the {@link IElement}
 * that was last added to the menu using a add() method.
 * Each page on the menu will track their own {@code previous}-slot if necessary.
 */
@FunctionalInterface
public interface PositionProvider {

    /** @param previous the previous position a element was added to on this page;
     *                   if no previous element was put on a page this argument will be null */
    SlotPos next(SlotPos previous);

    /** This is a default PositionProvider that tries to completely fill inventory rows.
     * One row is assumed to be 9 slots wide (since most UIs use a chest-like inventory).
     * For more details see <code>PositionProvider.Rows()</code> */
    PositionProvider DEFAULT_ROWS = Rows(0,9);
    /**
     * This Position Provider returns the {@link SlotPos} at the next inventory column or,
     * if lastColumn was reached a SlotPos at the next rows firstColumn.
     * The fill order is Left-to-Right; Top-to-Bottom.
     * After the 6th row was filled it will wrap back to the first row.
     * @param firstColumn the inclusive first column to reset to, if the row is full
     * @param lastColumn the exclusive last column, that determines when the row is full
     * @return a {@link PositionProvider} that fills rows
     */
    static PositionProvider Rows(int firstColumn, int lastColumn) {
        assert firstColumn <= lastColumn : "firstColumn can not exceed lastColumn";
        return previous -> {
            if (previous == null) return new SlotPos(0,0);
            int x = previous.getX()+1;
            int y = previous.getY();
            if (x >= lastColumn) {
                x = firstColumn;
                if (++y == 6)
                    y = 0;
            }
            return new SlotPos(x,y);
        };
    }

    /**
     * This Position Provider returns the {@link SlotPos} at the next inventory row or,
     * if lastRow was reached a SlotPos at the next columns firstRow.
     * The fill order is Top-to-Bottom; Left-to-Right.
     * After the 9th column was filled it will wrap back to the first column.
     * @param firstRow the inclusive first column to reset to, if the row is full
     * @param lastRow the exclusive last column, that determines when the row is full
     * @return a {@link PositionProvider} that fills rows
     */
    static PositionProvider Columns(int firstRow, int lastRow) {
        assert firstRow <= lastRow : "firstRow can not exceed lastRow";
        return previous -> {
            if (previous == null) return new SlotPos(0,0);
            int x = previous.getX();
            int y = previous.getY()+1;
            if (y >= lastRow) {
                y = firstRow;
                if (++x == 9)
                    x = 0;
            }
            return new SlotPos(x,y);
        };
    }

}
