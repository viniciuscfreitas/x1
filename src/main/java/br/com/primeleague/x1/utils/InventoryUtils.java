package br.com.primeleague.x1.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.enums.DuelType;

/**
 * Utilitários para manipulação de inventário
 */
public class InventoryUtils {

    /**
     * Verifica se o inventário de um jogador está vazio (sem itens)
     * 
     * @param player Jogador a verificar
     * @return true se o inventário estiver vazio, false caso contrário
     */
    public static boolean isInventoryEmpty(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (ItemStack item : contents) {
            if (item != null && item.getType() != Material.AIR) {
                return false;
            }
        }
        
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack item : armor) {
            if (item != null && item.getType() != Material.AIR) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Fornece o kit de duelo para um jogador
     * 
     * @param player Jogador
     * @param plugin Instância do plugin
     * @param duelType Tipo de duelo
     * @return true se o kit foi fornecido, false caso contrário
     */
    public static boolean giveDuelKit(Player player, Main plugin, DuelType duelType) {
        // Verificações de segurança para parâmetros nulos
        if (player == null) {
            System.out.println("[PrimeLeagueX1] ERRO: Player nulo ao tentar fornecer kit");
            return false;
        }
        
        if (plugin == null) {
            System.out.println("[PrimeLeagueX1] ERRO: Plugin nulo ao tentar fornecer kit para " + player.getName());
            return false;
        }
        
        // Verificar se o tipo de duelo é nulo
        if (duelType == null) {
            // Se for nulo, assumir que usa kit
            System.out.println("[PrimeLeagueX1] Tipo de duelo nulo, assumindo que usa kit para " + player.getName());
        } else if (!duelType.usesKit()) {
            return true; // Não precisa fornecer kit, jogador usa seus próprios itens
        }
        
        // Verificar ConfigManager e Config
        if (plugin.getConfigManager() == null) {
            System.out.println("[PrimeLeagueX1] ERRO: ConfigManager nulo ao tentar fornecer kit para " + player.getName());
            giveDefaultKit(player);
            return false;
        }
        
        // Usar método alternativo para acessar a seção "kit" diretamente
        try {
            System.out.println("[PrimeLeagueX1] Tentando fornecer kit para " + player.getName() + "...");
            
            // Verificar se o inventário deve estar vazio
            boolean checkEmptyInventory = false;
            try {
                checkEmptyInventory = plugin.getConfigManager().checkEmptyInventory();
            } catch (Exception e) {
                System.out.println("[PrimeLeagueX1] ERRO ao verificar configuração checkEmptyInventory: " + e.getMessage());
            }
            
            if (checkEmptyInventory && !isInventoryEmpty(player)) {
                try {
                    plugin.getMessageManager().sendMessage(player, "duelo.inventario-nao-vazio");
                } catch (Exception e) {
                    player.sendMessage("§cSeu inventário precisa estar vazio para receber o kit!");
                }
                return false;
            }
            
            // Vamos simplificar e dar diretamente kit completo de diamante se a configuração falhar
            // Primeira tentativa: buscar seção normalmente
            ConfigurationSection kitSection = null;
            
            if (plugin.getConfigManager().getConfig() != null) {
                kitSection = plugin.getConfigManager().getConfig().getConfigurationSection("kit");
                if (kitSection == null) {
                    System.out.println("[PrimeLeagueX1] Seção 'kit' não encontrada na configuração principal.");
                }
            } else {
                System.out.println("[PrimeLeagueX1] Configuração é nula.");
            }
            
            // Se não encontrou a seção kit, usar kit padrão
            if (kitSection == null) {
                System.out.println("[PrimeLeagueX1] Usando kit padrão para " + player.getName());
                giveDefaultKit(player);
                return true;
            }
            
            // Limpar inventário do jogador antes de dar kit
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            
            // Continuar processamento apenas se encontrou a seção
            // Kit de armadura
            if (kitSection.contains("armor")) {
                ConfigurationSection armorSection = kitSection.getConfigurationSection("armor");
                
                if (armorSection != null) {
                    // Capacete
                    if (armorSection.contains("helmet")) {
                        ConfigurationSection helmetSection = armorSection.getConfigurationSection("helmet");
                        if (helmetSection != null) {
                            ItemStack helmet = createItemFromConfig(helmetSection);
                            if (helmet != null) {
                                player.getInventory().setHelmet(helmet);
                            }
                        }
                    }
                    
                    // Peitoral
                    if (armorSection.contains("chestplate")) {
                        ConfigurationSection chestplateSection = armorSection.getConfigurationSection("chestplate");
                        if (chestplateSection != null) {
                            ItemStack chestplate = createItemFromConfig(chestplateSection);
                            if (chestplate != null) {
                                player.getInventory().setChestplate(chestplate);
                            }
                        }
                    }
                    
                    // Calças
                    if (armorSection.contains("leggings")) {
                        ConfigurationSection leggingsSection = armorSection.getConfigurationSection("leggings");
                        if (leggingsSection != null) {
                            ItemStack leggings = createItemFromConfig(leggingsSection);
                            if (leggings != null) {
                                player.getInventory().setLeggings(leggings);
                            }
                        }
                    }
                    
                    // Botas
                    if (armorSection.contains("boots")) {
                        ConfigurationSection bootsSection = armorSection.getConfigurationSection("boots");
                        if (bootsSection != null) {
                            ItemStack boots = createItemFromConfig(bootsSection);
                            if (boots != null) {
                                player.getInventory().setBoots(boots);
                            }
                        }
                    }
                }
            }
            
            // Kit de itens
            if (kitSection.contains("items")) {
                ConfigurationSection itemsSection = kitSection.getConfigurationSection("items");
                
                if (itemsSection != null) {
                    for (String key : itemsSection.getKeys(false)) {
                        ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                        
                        if (itemSection != null) {
                            ItemStack item = createItemFromConfig(itemSection);
                            
                            if (item != null) {
                                int slot = itemSection.getInt("slot", -1);
                                
                                if (slot >= 0 && slot < 36) {
                                    player.getInventory().setItem(slot, item);
                                } else {
                                    player.getInventory().addItem(item);
                                }
                            }
                        }
                    }
                }
            }
            
            player.updateInventory();
            System.out.println("[PrimeLeagueX1] Kit fornecido com sucesso para " + player.getName());
            return true;
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] ERRO ao fornecer kit para " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            giveDefaultKit(player);
            return false;
        }
    }
    
    /**
     * Cria um item a partir de uma seção de configuração
     * 
     * @param itemSection Seção de configuração do item
     * @return Item criado ou null se falhar
     */
    private static ItemStack createItemFromConfig(ConfigurationSection itemSection) {
        if (itemSection == null) {
            return null;
        }
        
        String materialName = itemSection.getString("material");
        if (materialName == null || materialName.isEmpty()) {
            return null;
        }
        
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            return null;
        }
        
        int amount = itemSection.getInt("amount", 1);
        short data = (short) itemSection.getInt("data", 0);
        
        ItemStack item = new ItemStack(material, amount, data);
        ItemMeta meta = item.getItemMeta();
        
        // Nome personalizado
        String name = itemSection.getString("name");
        if (name != null && !name.isEmpty()) {
            meta.setDisplayName(ensureValidLength(name));
        }
        
        // Lore (descrição)
        List<String> lore = itemSection.getStringList("lore");
        if (!lore.isEmpty()) {
            List<String> colorizedLore = new ArrayList<String>();
            for (String line : lore) {
                colorizedLore.add(ColorUtils.colorize(line));
            }
            meta.setLore(colorizedLore);
        }
        
        item.setItemMeta(meta);
        
        // Encantamentos
        if (itemSection.contains("enchantments")) {
            ConfigurationSection enchantmentsSection = itemSection.getConfigurationSection("enchantments");
            if (enchantmentsSection != null) {
                for (String enchantName : enchantmentsSection.getKeys(false)) {
                    Enchantment enchant = Enchantment.getByName(enchantName);
                    if (enchant != null) {
                        int level = enchantmentsSection.getInt(enchantName);
                        item.addUnsafeEnchantment(enchant, level);
                    }
                }
            }
        }
        
        return item;
    }
    
    /**
     * Fornece o kit padrão para um jogador
     * 
     * @param player Jogador
     */
    private static void giveDefaultKit(Player player) {
        if (player == null) {
            System.out.println("[PrimeLeagueX1] ERRO: Tentativa de dar kit padrão para player nulo");
            return;
        }
        
        try {
            System.out.println("[PrimeLeagueX1] Fornecendo kit padrão para " + player.getName());
            
            // Limpar inventário primeiro
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            
            // Armadura de ferro
            ItemStack helmet = new ItemStack(Material.IRON_HELMET);
            ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE);
            ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS);
            ItemStack boots = new ItemStack(Material.IRON_BOOTS);
            
            // Aplicar itens de armadura
            player.getInventory().setHelmet(helmet);
            player.getInventory().setChestplate(chestplate);
            player.getInventory().setLeggings(leggings);
            player.getInventory().setBoots(boots);
            
            // Espada de ferro
            ItemStack sword = new ItemStack(Material.IRON_SWORD);
            ItemMeta swordMeta = sword.getItemMeta();
            if (swordMeta != null) {
                swordMeta.setDisplayName(ensureValidLength("&6Espada do Duelo"));
                sword.setItemMeta(swordMeta);
            }
            player.getInventory().setItem(0, sword);
            
            // Maçãs douradas
            ItemStack apples = new ItemStack(Material.GOLDEN_APPLE, 5);
            player.getInventory().setItem(1, apples);
            
            // Poções
            try {
                ItemStack healthPotion = new ItemStack(Material.POTION, 3, (short) 16389); // Poção de cura instantânea
                player.getInventory().setItem(2, healthPotion);
            } catch (Exception e) {
                System.out.println("[PrimeLeagueX1] Erro ao criar poção para o kit: " + e.getMessage());
            }
            
            player.updateInventory();
            System.out.println("[PrimeLeagueX1] Kit padrão fornecido com sucesso para " + player.getName());
        } catch (Exception e) {
            System.out.println("[PrimeLeagueX1] ERRO ao fornecer kit padrão para " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Verifica se um nome de item excede o limite do Minecraft 1.5.2 (32 caracteres)
     * e realiza o truncamento se necessário.
     * 
     * @param name Nome a ser verificado
     * @return Nome truncado se necessário
     */
    public static String ensureValidLength(String name) {
        if (name == null) {
            return null;
        }
        
        String colorized = ColorUtils.colorize(name);
        if (colorized.length() > 32) {
            System.out.println("[PrimeLeagueX1] AVISO: Nome de item muito longo truncado: " + colorized);
            return colorized.substring(0, 32);
        }
        
        return colorized;
    }
    
    /**
     * Cria um item com nome e descrição personalizados
     * 
     * @param material Material do item
     * @param name Nome do item
     * @param lore Descrição do item
     * @return Item criado
     */
    public static ItemStack createNamedItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (name != null) {
            // Garantir que o nome não exceda o limite
            meta.setDisplayName(ensureValidLength(name));
        }
        
        if (lore != null && lore.length > 0) {
            // Usar ArrayList em vez de Stream (compatível com Java 7)
            List<String> colorizedLore = new ArrayList<String>();
            for (String line : lore) {
                colorizedLore.add(ColorUtils.colorize(line));
            }
            meta.setLore(colorizedLore);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Cria um item com nome, durabilidade e descrição personalizados
     * 
     * @param material Material do item
     * @param durability Durabilidade/data value do item
     * @param name Nome do item
     * @param lore Descrição do item
     * @return Item criado
     */
    public static ItemStack createNamedItem(Material material, short durability, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1, durability);
        ItemMeta meta = item.getItemMeta();
        
        if (name != null) {
            // Garantir que o nome não exceda o limite
            meta.setDisplayName(ensureValidLength(name));
        }
        
        if (lore != null && lore.length > 0) {
            // Usar ArrayList em vez de Stream (compatível com Java 7)
            List<String> colorizedLore = new ArrayList<String>();
            for (String line : lore) {
                colorizedLore.add(ColorUtils.colorize(line));
            }
            meta.setLore(colorizedLore);
        }
        
        item.setItemMeta(meta);
        return item;
    }
} 