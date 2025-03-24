package br.com.primeleague.x1.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import br.com.primeleague.x1.Main;
import net.milkbowl.vault.economy.Economy;

/**
 * Gerenciador de economia do plugin
 */
public class EconomyManager {

    private final Main plugin;
    private Economy economy;
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public EconomyManager(Main plugin) {
        this.plugin = plugin;
        setupEconomy();
    }
    
    /**
     * Configura a economia através do Vault
     * 
     * @return true se a configuração foi bem-sucedida
     */
    private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault não encontrado! O sistema de apostas estará desativado.");
            return false;
        }
        
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("Serviço de economia não encontrado! O sistema de apostas estará desativado.");
            return false;
        }
        
        economy = rsp.getProvider();
        return economy != null;
    }
    
    /**
     * Verifica se a economia está disponível
     * 
     * @return true se a economia está disponível
     */
    public boolean isEconomyEnabled() {
        return economy != null;
    }
    
    /**
     * Obtém o saldo de um jogador
     * 
     * @param playerName Nome do jogador
     * @return Saldo do jogador
     */
    public double getBalance(String playerName) {
        if (!isEconomyEnabled()) {
            return 0;
        }
        
        return economy.getBalance(playerName);
    }
    
    /**
     * Verifica se um jogador tem saldo suficiente
     * 
     * @param playerName Nome do jogador
     * @param amount Valor a verificar
     * @return true se o jogador tem saldo suficiente
     */
    public boolean hasEnoughMoney(String playerName, double amount) {
        if (!isEconomyEnabled()) {
            return false;
        }
        
        return economy.has(playerName, amount);
    }
    
    /**
     * Verifica se um jogador tem saldo suficiente
     * 
     * @param player Jogador
     * @param amount Valor a verificar
     * @return true se o jogador tem saldo suficiente
     */
    public boolean hasEnoughMoney(Player player, double amount) {
        if (player == null) {
            return false;
        }
        
        return hasEnoughMoney(player.getName(), amount);
    }
    
    /**
     * Retira dinheiro de um jogador
     * 
     * @param playerName Nome do jogador
     * @param amount Valor a retirar
     * @return true se a operação foi bem-sucedida
     */
    public boolean withdrawMoney(String playerName, double amount) {
        if (!isEconomyEnabled()) {
            return false;
        }
        
        return economy.withdrawPlayer(playerName, amount).transactionSuccess();
    }
    
    /**
     * Retira dinheiro de um jogador
     * 
     * @param player Jogador
     * @param amount Valor a retirar
     * @return true se a operação foi bem-sucedida
     */
    public boolean withdrawMoney(Player player, double amount) {
        if (player == null) {
            return false;
        }
        
        return withdrawMoney(player.getName(), amount);
    }
    
    /**
     * Deposita dinheiro para um jogador
     * 
     * @param playerName Nome do jogador
     * @param amount Valor a depositar
     * @return true se a operação foi bem-sucedida
     */
    public boolean depositMoney(String playerName, double amount) {
        if (!isEconomyEnabled()) {
            return false;
        }
        
        return economy.depositPlayer(playerName, amount).transactionSuccess();
    }
    
    /**
     * Deposita dinheiro para um jogador
     * 
     * @param player Jogador
     * @param amount Valor a depositar
     * @return true se a operação foi bem-sucedida
     */
    public boolean depositMoney(Player player, double amount) {
        if (player == null) {
            return false;
        }
        
        return depositMoney(player.getName(), amount);
    }
    
    /**
     * Formata um valor monetário
     * 
     * @param amount Valor a formatar
     * @return Valor formatado
     */
    public String formatMoney(double amount) {
        if (!isEconomyEnabled()) {
            return String.valueOf(amount);
        }
        
        return economy.format(amount);
    }
    
    /**
     * Mensagem de erro para jogador sem dinheiro suficiente
     * 
     * @param player Jogador
     * @param amount Valor
     */
    public void sendNotEnoughMoneyMessage(Player player, double amount) {
        if (player == null) {
            return;
        }
        
        plugin.getMessageManager().sendMessage(player, "economia.sem-dinheiro", "%valor%", formatMoney(amount));
    }
} 