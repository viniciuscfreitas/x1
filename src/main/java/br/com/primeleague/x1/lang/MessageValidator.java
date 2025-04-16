package br.com.primeleague.x1.lang;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;

import br.com.primeleague.x1.managers.MessageManager;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Classe responsável por validar as mensagens do plugin
 */
public class MessageValidator {
    
    /**
     * Valores padrão para mensagens essenciais
     */
    private static final Map<String, String> MENSAGENS_PADRAO = new HashMap<String, String>() {{
        put("prefixo", "&6[&c*&6] &fX1 &6[&c*&6] &r");
        put("geral.plugin-ativado", "&aO plugin foi ativado com sucesso!");
        put("geral.plugin-desativado", "&cO plugin foi desativado!");
        put("geral.sem-permissao", "&c! Você não tem permissão para isso!");
        put("geral.comando-inexistente", "&cEste comando não existe! Use &f/x1 ajuda &cpara ver os comandos disponíveis.");
        put("geral.reload-completo", "&aConfigurações e mensagens recarregadas com sucesso!");
        put("duelo.desafio-enviado", "&a+ Você desafiou &f%player% &apara um &e%tipo%&a!");
        put("duelo.desafio-aceito", "&a+ Você aceitou o desafio de &f%player%&a!");
        put("duelo.vitoria", "&a+ Você venceu o duelo contra &f%player%&a!");
        put("duelo.derrota", "&c- Você perdeu o duelo contra &f%player%&c!");
        put("scoreboard.enabled", "true");
        put("scoreboard.titulo", "&eX1 PrimeLeague");
        put("scoreboard.preduel.title", "&eAguardando duelo");
        put("scoreboard.duel.title", "&6X1 em andamento");
        put("scoreboard.team.title", "&6Duelo de Times");
        put("scoreboard.end.title", "&aDuelo Finalizado");
        put("gui.titulo-principal", "&8● &c* &fMenu de Duelos &c* &8●");
        put("gui.item-desafiar", "&cDesafiar Jogador");
        put("gui.fechar", "&cFechar");
    }};
    
    /**
     * Valida todas as mensagens essenciais do plugin e adiciona as ausentes
     * 
     * @param messageManager Gerenciador de mensagens
     * @return true se todas as mensagens estiverem presentes
     */
    public static boolean validarMensagensEssenciais(MessageManager messageManager) {
        if (messageManager == null) {
            Bukkit.getLogger().warning("[PrimeLeagueX1] MessageManager não inicializado. Não é possível validar mensagens.");
            return false;
        }
        
        List<String> mensagensAusentes = new ArrayList<String>();
        int total = 0;
        int adicionadas = 0;
        
        Bukkit.getLogger().info("[PrimeLeagueX1] Validando mensagens do plugin...");
        
        try {
            // Obtém todos os campos da classe MessageKeys usando reflection
            Field[] campos = MessageKeys.class.getDeclaredFields();
            
            for (Field campo : campos) {
                // Verifica se o campo é uma constante String
                if (campo.getType() == String.class) {
                    total++;
                    
                    // Obtém o valor da constante
                    String chave = (String) campo.get(null);
                    
                    // Verifica se a mensagem existe no MessageManager
                    if (!messageManager.hasMessage(chave)) {
                        mensagensAusentes.add(chave);
                        
                        // Adicionar automaticamente a mensagem se tiver valor padrão definido
                        if (MENSAGENS_PADRAO.containsKey(chave)) {
                            String valorPadrao = MENSAGENS_PADRAO.get(chave);
                            // Adicionar ao arquivo de mensagens
                            adicionarMensagemAusente(messageManager, chave, valorPadrao);
                            adicionadas++;
                            Bukkit.getLogger().info("[PrimeLeagueX1] Mensagem ausente adicionada automaticamente: " + chave);
                        } else {
                            // Para mensagens sem valor padrão, criar um valor genérico
                            String categoria = chave.contains(".") ? chave.substring(0, chave.indexOf(".")) : "geral";
                            String valorPadrao = "&7Mensagem " + categoria + " (não configurada)";
                            adicionarMensagemAusente(messageManager, chave, valorPadrao);
                            adicionadas++;
                            Bukkit.getLogger().info("[PrimeLeagueX1] Mensagem ausente adicionada com valor genérico: " + chave);
                        }
                    }
                }
            }
            
            // Verificar mensagens específicas importantes que podem não estar na classe MessageKeys
            String[] mensagensImportantes = {
                "scoreboard.enabled",
                "scoreboard.titulo",
                "scoreboard.preduel.title",
                "scoreboard.duel.title",
                "scoreboard.team.title",
                "scoreboard.end.title",
                "geral.reload-completo"
            };
            
            for (String chave : mensagensImportantes) {
                if (!messageManager.hasMessage(chave)) {
                    if (MENSAGENS_PADRAO.containsKey(chave)) {
                        String valorPadrao = MENSAGENS_PADRAO.get(chave);
                        adicionarMensagemAusente(messageManager, chave, valorPadrao);
                        adicionadas++;
                        Bukkit.getLogger().info("[PrimeLeagueX1] Mensagem importante ausente adicionada automaticamente: " + chave);
                    }
                }
            }
            
            if (adicionadas > 0) {
                Bukkit.getLogger().info("[PrimeLeagueX1] Foram adicionadas " + adicionadas + " mensagens ausentes ao arquivo!");
                // Recarregar as mensagens para que as recém adicionadas sejam carregadas
                messageManager.reload();
            }
            
            if (mensagensAusentes.isEmpty() || adicionadas == mensagensAusentes.size()) {
                Bukkit.getLogger().info("[PrimeLeagueX1] Todas as " + total + " mensagens foram validadas com sucesso!");
                return true;
            } else {
                Bukkit.getLogger().warning("[PrimeLeagueX1] " + (mensagensAusentes.size() - adicionadas) + " mensagens ainda estão ausentes de " + total + " mensagens verificadas:");
                
                for (String mensagem : mensagensAusentes) {
                    if (!MENSAGENS_PADRAO.containsKey(mensagem)) {
                        Bukkit.getLogger().warning("  - Mensagem ausente sem valor padrão: " + mensagem);
                    }
                }
                return false;
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("[PrimeLeagueX1] Erro ao validar mensagens: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Adiciona uma mensagem ausente diretamente ao arquivo de configuração
     * 
     * @param messageManager Gerenciador de mensagens
     * @param chave Chave da mensagem
     * @param valorPadrao Valor padrão para a mensagem
     */
    private static void adicionarMensagemAusente(MessageManager messageManager, String chave, String valorPadrao) {
        FileConfiguration config = messageManager.getMessageConfig();
        config.set(chave, valorPadrao);
        messageManager.saveMessages();
    }
} 