package br.com.primeleague.x1.managers;

import java.util.HashMap;
import java.util.Map;

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
     * Envia mensagem de dinheiro insuficiente para o jogador
     * 
     * @param player Jogador
     * @param amount Valor
     */
    public void sendNotEnoughMoneyMessage(Player player, double amount) {
        if (player == null) {
            return;
        }
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("valor", formatMoney(amount));
        plugin.getMessageManager().sendMessage(player, "economia.sem-dinheiro", placeholders);
    }

    /**
     * Processa a perda de uma aposta
     * 
     * @param player Jogador
     * @param amount Valor
     */
    public void processDuelLoss(Player player, double amount) {
        if (economy == null || !isEconomyEnabled()) {
            return;
        }
        
        // A quantia já foi descontada antes do duelo, não precisa fazer nada
    }

    /**
     * Verifica se um jogador tem dinheiro suficiente
     * 
     * @param player Jogador
     * @param amount Valor
     * @return true se tiver dinheiro suficiente, false caso contrário
     */
    public boolean hasMoney(Player player, double amount) {
        if (economy == null || !isEconomyEnabled()) {
            return true; // Se a economia estiver desativada, considera que tem dinheiro
        }
        
        return economy.has(player.getName(), amount);
    }

    /**
     * Devolve o dinheiro de uma aposta em caso de empate ou cancelamento
     * 
     * @param player Jogador
     * @param amount Valor
     */
    public void returnBet(Player player, double amount) {
        if (economy == null || !isEconomyEnabled()) {
            return;
        }
        
        economy.depositPlayer(player.getName(), amount);
    }

    /**
     * Processa a vitória de um duelo, adicionando o valor da aposta para o vencedor
     * 
     * @param player Jogador vencedor
     * @param amount Valor total (2x o valor apostado)
     */
    public void processDuelWin(Player player, double amount) {
        if (economy == null || !isEconomyEnabled()) {
            return;
        }
        
        // A metade já foi descontada antes do duelo, agora deposita o prêmio completo
        economy.depositPlayer(player.getName(), amount);
    }

    /**
     * Deposita dinheiro para um jogador (alias para depositMoney)
     * 
     * @param playerName Nome do jogador
     * @param amount Valor a depositar
     * @return true se a operação foi bem-sucedida
     */
    public boolean giveMoney(String playerName, double amount) {
        return depositMoney(playerName, amount);
    }
} 