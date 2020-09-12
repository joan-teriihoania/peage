package fr.joanteriihoania.peage;

import org.bukkit.entity.Player;

public class EconomyCustom {
    private static Main main;

    public static void setMainInstance(Main main) {
        EconomyCustom.main = main;
    }

    public static boolean withdraw(Player player, double money){
        if (!canPurchase(player, money)) return false;
        return main.economy.withdrawPlayer(player, money).transactionSuccess();
    }

    public static boolean deposit(Player player, double money){
        return main.economy.depositPlayer(player, money).transactionSuccess();
    }

    public static boolean transfer(Player playerFrom, Player playerTo, double money){
        if(withdraw(playerFrom, money)) {
            deposit(playerTo, money);
            return true;
        }
        return false;
    }

    public static boolean canPurchase(Player player, double money){
        return main.economy.getBalance(player) >= money;
    }
}
