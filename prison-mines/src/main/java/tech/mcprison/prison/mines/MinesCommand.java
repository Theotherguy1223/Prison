/*
 *  Prison is a Minecraft plugin for the prison game mode.
 *  Copyright (C) 2016 The Prison Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tech.mcprison.prison.mines;

import tech.mcprison.prison.Prison;
import tech.mcprison.prison.commands.Arg;
import tech.mcprison.prison.commands.Command;
import tech.mcprison.prison.internal.Player;
import tech.mcprison.prison.selection.Selection;

/**
 * @author SirFaizdat
 */
public class MinesCommand {

    private MinesModule minesModule;

    public MinesCommand(MinesModule minesModule) {
        this.minesModule = minesModule;
    }

    @Command(identifier = "mines create", description = "Create a new mine from your current selection.")
    public void createCommand(Player player, @Arg(name = "name") String name) {
        Selection sel = Prison.getInstance().getSelectionManager().getSelection(player);
        if (!sel.isComplete()) {
            player.sendMessage(Prison.getInstance().getMessages().selectionNeeded);
            return;
        }

        Mine mine = new Mine(name, sel.asBounds());
        this.minesModule.saveMine(mine);
        this.minesModule.getResetTimer().registerIndividualMine(mine);
        player.sendMessage(String.format(this.minesModule.getMessages().mineCreated, name));
    }

}