package br.com.primeleague.x1.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.managers.MessageManager;

/**
 * Classe utilitária para criar e gerenciar scoreboards facilmente
 * Otimizada para Bukkit 1.5.2
 */
public class ScoreHelper {

    private static Map<String, ScoreHelper> helpers = new HashMap<String, ScoreHelper>();
    private static Map<String, Map<Integer, String>> linhasAtuais = new HashMap<String, Map<Integer, String>>();
    private static Map<String, String> truncadosRegistrados = new HashMap<String, String>();
    private static Plugin plugin;
    
    private String playerName;
    private Scoreboard scoreboard;
    private Objective objective;
    private String scoreboardTitle;
    private Map<Integer, String> linhasEntradas = new HashMap<>();
    
    private final Main mainPlugin;
    private final MessageManager messageManager;
    private final Map<Player, Scoreboard> playerScoreboards;
    private final Map<Player, Objective> playerObjectives;
    
    /**
     * Construtor
     * 
     * @param plugin Instância do plugin
     */
    public ScoreHelper(Main plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin não pode ser nulo");
        }
        this.mainPlugin = plugin;
        this.messageManager = plugin.getMessageManager();
        this.playerScoreboards = new HashMap<>();
        this.playerObjectives = new HashMap<>();
    }
    
    /**
     * Inicializa o sistema de Score Helper com a instância do plugin
     * 
     * @param mainPlugin Instância do plugin principal
     */
    public static void initialize(Plugin mainPlugin) {
        plugin = mainPlugin;
        System.out.println("[ScoreHelper] Inicializado com sucesso para plugin: " + mainPlugin.getName());
    }
    
    /**
     * Cria um novo ScoreHelper para o jogador
     * 
     * @param player Jogador para criar o ScoreHelper
     * @param titulo Nome a ser exibido na scoreboard
     */
    public ScoreHelper(Player player, String titulo) {
        // Inicializar as variáveis finais para evitar erro de compilação
        this.mainPlugin = null;
        this.messageManager = null;
        this.playerScoreboards = null;
        this.playerObjectives = null;
        
        String title = ColorUtils.colorize(titulo);
        this.playerName = player != null ? player.getName() : "offline";
        
        // Verificar tamanho do título (máximo 32 caracteres)
        if (title.length() > 32) {
            title = title.substring(0, 32);
            System.out.println("[ScoreHelper] Título truncado: " + titulo + " -> " + title);
        }
        
        this.scoreboardTitle = title;
        
        // Inicializar cache de linhas para este jogador
        if (!linhasAtuais.containsKey(playerName)) {
            linhasAtuais.put(playerName, new HashMap<Integer, String>());
        }
        
        // Se o plugin estiver inicializado e o jogador estiver online, configurar scoreboard
        if (player != null && player.isOnline()) {
            try {
                if (plugin != null) {
                    // Aplicar scoreboard com delay para garantir que tudo esteja pronto
                    final String finalTitle = title;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                aplicarScoreboard(player, finalTitle);
                                System.out.println("[DEBUG] Scoreboard aplicada com sucesso para " + player.getName());
                            } catch (Exception e) {
                                System.out.println("[ERRO] Falha ao aplicar scoreboard com delay: " + e.getMessage());
                            }
                        }
                    }.runTaskLater(plugin, 1L); // 1 tick de delay
                } else {
                    // Aplicar diretamente se o plugin não estiver inicializado
                    aplicarScoreboard(player, title);
                }
            } catch (Exception e) {
                System.out.println("[ERRO] Falha ao inicializar ScoreHelper: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("[AVISO] Tentativa de criar ScoreHelper para jogador offline: " + playerName);
        }
    }
    
    /**
     * Aplica a scoreboard ao jogador
     */
    private void aplicarScoreboard(Player player, String title) {
        if (player == null || !player.isOnline()) {
            System.out.println("[ScoreHelper] ERRO: Tentativa de aplicar scoreboard para jogador null ou offline");
            return;
        }
        
        try {
            // Obter ScoreboardManager do Bukkit
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            if (manager == null) {
                System.out.println("[ScoreHelper] ERRO: ScoreboardManager do Bukkit é null!");
                return;
            }
            
            // Criar nova scoreboard
            this.scoreboard = manager.getNewScoreboard();
            if (this.scoreboard == null) {
                System.out.println("[ScoreHelper] ERRO: Não foi possível criar nova scoreboard!");
                return;
            }
            
            // Criar e configurar objetivo
            this.objective = scoreboard.registerNewObjective("x1board", "dummy");
            if (this.objective == null) {
                System.out.println("[ScoreHelper] ERRO: Não foi possível registrar novo objetivo!");
                return;
            }
            
            // Definir nome e slot do objetivo
            this.objective.setDisplayName(title);
            this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            
            // Limpar qualquer scoreboard atual que possa estar interferindo
            limparScoreboardAnterior(player);
            
            // Aplicar ao jogador
            player.setScoreboard(scoreboard);
            
            // Logar aplicação bem-sucedida
            System.out.println("[DEBUG] Scoreboard '" + title + "' aplicada com sucesso para " + player.getName());
        } catch (Exception e) {
            System.out.println("[ScoreHelper] ERRO ao aplicar scoreboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Limpa a scoreboard anterior do jogador, se existir
     */
    private void limparScoreboardAnterior(Player player) {
        try {
            // Verificar se o jogador tem uma scoreboard atribuída
            Scoreboard atual = player.getScoreboard();
            if (atual != null) {
                // Verificar se é uma scoreboard vazia
                boolean vazia = true;
                Objective obj = atual.getObjective(DisplaySlot.SIDEBAR);
                if (obj != null) {
                    // Se já existir uma objective na SIDEBAR, verificar se é nossa
                    if (obj.getName().equals("x1board")) {
                        // Se for nossa, verificar se tem entradas
                        for (Team team : atual.getTeams()) {
                            if (team.getName().startsWith("x1_")) {
                                vazia = false;
                                break;
                            }
                        }
                        
                        // Se estiver vazia, unregister todos os times x1_ para evitar limitações
                        if (vazia) {
                            for (Team team : atual.getTeams()) {
                                if (team.getName().startsWith("x1_")) {
                                    team.unregister();
                                }
                            }
                            System.out.println("[DEBUG] Times da scoreboard anterior limpos para " + player.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[AVISO] Erro ao tentar limpar scoreboard anterior: " + e.getMessage());
        }
    }
    
    /**
     * Obtém ou cria um ScoreHelper para o jogador
     * 
     * @param player Jogador para obter o ScoreHelper
     * @param displayName Nome a ser exibido na scoreboard
     * @return ScoreHelper para o jogador
     */
    public static ScoreHelper getHelper(Player player, String displayName) {
        if (player == null || !player.isOnline()) {
            System.out.println("[ScoreHelper] Tentativa de criar scoreboard para jogador offline");
            return null;
        }
        
        boolean recriar = false;
        boolean atualizar = false;
        
        // Primeiro, verificar se já existe um helper para este jogador
        if (helpers.containsKey(player.getName())) {
            ScoreHelper helper = helpers.get(player.getName());
            
            try {
                // Verificar se o helper está em estado válido
                if (helper == null || helper.scoreboard == null || helper.objective == null) {
                    System.out.println("[DEBUG] Helper inválido para " + player.getName() + ", recriando...");
                    recriar = true;
                } else {
                    // Verificar se o objetivo ainda existe na scoreboard
                    if (helper.scoreboard.getObjective(helper.objective.getName()) == null) {
                        System.out.println("[DEBUG] Objetivo não existe mais na scoreboard para " + player.getName() + ", recriando...");
                        recriar = true;
                    } else {
                        // Atualizar título caso seja diferente
                        String displayNameColorized = ColorUtils.colorize(displayName);
                        if (displayNameColorized.length() > 32) {
                            displayNameColorized = displayNameColorized.substring(0, 32);
                        }
                        
                        if (!helper.objective.getDisplayName().equals(displayNameColorized)) {
                            System.out.println("[DEBUG] Atualizando título de scoreboard para " + player.getName());
                            helper.objective.setDisplayName(displayNameColorized);
                            atualizar = true;
                        }
                        
                        // Se não for para recriar, retornar o helper existente com título atualizado
                        if (!recriar) {
                            return helper;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("[ERRO] Falha ao verificar helper existente: " + e.getMessage());
                recriar = true;
            }
            
            // Se precisar recriar, remover o helper atual
            if (recriar) {
                try {
                    helpers.remove(player.getName());
                    // Não remover linhasAtuais para manter cache de conteúdo
                } catch (Exception e) {
                    System.out.println("[ERRO] Falha ao remover helper antigo: " + e.getMessage());
                }
            }
        }
        
        // Se não existe ou precisa recriar, criar um novo helper
        try {
            System.out.println("[DEBUG] Criando novo ScoreHelper para " + player.getName());
            ScoreHelper helper = new ScoreHelper(player, displayName);
            helpers.put(player.getName(), helper);
            return helper;
        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao criar novo ScoreHelper: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Remove o ScoreHelper do jogador
     * 
     * @param playerName Nome do jogador
     */
    public static void removeHelper(String playerName) {
        helpers.remove(playerName);
        linhasAtuais.remove(playerName);
    }
    
    /**
     * Verifica se o jogador tem um ScoreHelper ativo
     * 
     * @param playerName Nome do jogador
     * @return true se o jogador tiver um ScoreHelper
     */
    public static boolean hasHelper(String playerName) {
        return helpers.containsKey(playerName);
    }
    
    /**
     * Define uma linha na scoreboard
     * 
     * @param linha Texto da linha (máximo 16 caracteres após colorizado)
     * @param valor Valor/posição da linha (maior = mais alto)
     */
    public void setLinha(String linha, int valor) {
        // Garantir limite de 16 caracteres por linha
        String linhaFinal = linha;
        String chaveLog = playerName + "_" + valor;
        
        if (linhaFinal.length() > 40) {
            // Se a linha tem apenas códigos de cor, precisamos garantir que pelo menos um caractere visível seja mantido
            String strippedLine = ChatColor.stripColor(linhaFinal);
            if (strippedLine.isEmpty() || strippedLine.length() == 0) {
                // Se a linha tem apenas cores, usar um texto padrão
                linhaFinal = "&f...";
                System.out.println("[AVISO] Linha invisível detectada. Usando fallback: " + linhaFinal);
            } else {
                // Caso contrário, truncar a partir da posição 16, mas garantir ao menos 1 caractere visível
                linhaFinal = garantirLinhaVisivel(linhaFinal);
            }
            
            // Evitar flood de logs registrando apenas uma vez por linha/jogador
            if (!truncadosRegistrados.containsKey(chaveLog)) {
                System.out.println("[ScoreHelper] Linha truncada para scoreboard: " + linha + " -> " + linhaFinal);
                truncadosRegistrados.put(chaveLog, linhaFinal);
            }
        }
        
        // Verificar se a linha é diferente da atual para evitar atualizações desnecessárias
        Map<Integer, String> linhasDoJogador = linhasAtuais.get(playerName);
        if (linhasDoJogador != null && linhasDoJogador.containsKey(valor)) {
            String linhaAtual = linhasDoJogador.get(valor);
            if (linhaAtual.equals(linhaFinal)) {
                return; // Linha não mudou, não precisa atualizar
            }
        }
        
        // Armazenar a linha nos mapas de cache
        if (linhasDoJogador != null) {
            linhasDoJogador.put(valor, linhaFinal);
        }
        linhasEntradas.put(valor, linhaFinal);
        
        // Criar ou atualizar a entrada na scoreboard
        createOrUpdateEntry(linhaFinal, valor);
    }
    
    /**
     * Garante que a linha tenha pelo menos um caractere visível após truncamento
     * 
     * @param linha Linha a ser verificada
     * @return Linha com pelo menos um caractere visível
     */
    private String garantirLinhaVisivel(String linha) {
        if (linha == null || linha.isEmpty()) {
            return "&f...";
        }
        
        // Se a linha é maior que 16, truncar mas garantir que há pelo menos um caractere visível
        if (linha.length() > 40) {
            // Encontrar a última sequência de cor antes da posição 16
            String cores = "";
            boolean encontrouCor = false;
            
            for (int i = 0; i < Math.min(40, linha.length() - 1); i++) {
                if (linha.charAt(i) == '§' && i + 1 < linha.length()) {
                    cores = linha.substring(i, i + 2);
                    encontrouCor = true;
                    i++; // Pular o próximo caractere que é o código da cor
                }
            }
            
            // Extrair texto visível (sem cores)
            String textoVisivel = ChatColor.stripColor(linha);
            
            // Se não há texto visível, usar fallback
            if (textoVisivel.isEmpty()) {
                return "&f...";
            }
            
            // Se o texto visível é maior que 37 caracteres, truncar para deixar espaço para "..."
            if (textoVisivel.length() > 37) {
                textoVisivel = textoVisivel.substring(0, 37) + "...";
            }
            
            // Reconstruir com cores + texto visível truncado, limitado a 40 caracteres
            String resultado = (encontrouCor ? cores : "") + textoVisivel;
            
            // Garantir que não exceda 40 caracteres
            if (resultado.length() > 40) {
                resultado = resultado.substring(0, 40);
            }
            
            return resultado;
        }
        
        return linha;
    }
    
    /**
     * Cria ou atualiza uma entrada na scoreboard usando Teams para minimizar o flicker
     * 
     * @param texto Texto da linha
     * @param valor Valor/posição da linha
     */
    private void createOrUpdateEntry(String texto, int valor) {
        // Verificar se o objetivo está válido
        if (objective == null) {
            System.out.println("[ScoreHelper] ERRO: Objetivo é null para jogador " + playerName);
            // Tentar recriar o objetivo se possível
            if (scoreboard != null) {
                try {
                    System.out.println("[ScoreHelper] Tentando recriar objetivo para " + playerName);
                    objective = scoreboard.registerNewObjective("x1board", "dummy");
                    objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                    objective.setDisplayName(this.scoreboardTitle != null ? this.scoreboardTitle : "X1");
                } catch (Exception e) {
                    System.out.println("[ScoreHelper] ERRO ao recriar objetivo: " + e.getMessage());
                    return;
                }
            } else {
                System.out.println("[ScoreHelper] ERRO: Scoreboard também é null para " + playerName);
                return;
            }
        }
        
        // Verificar se o scoreboard está válido
        if (scoreboard == null) {
            System.out.println("[ScoreHelper] ERRO: Scoreboard é null para jogador " + playerName);
            return;
        }
        
        // Garantir que o texto nunca seja nulo ou vazio
        if (texto == null || texto.isEmpty() || texto.replace("§", "").isEmpty()) {
            texto = "§f..."; // Fallback para texto vazio
        }
        
        try {
            // Criar uma entrada única para esta posição
            String entryName = getEntryName(valor);
            
            // Obter ou criar o team para esta entrada
            String teamName = "x1_" + valor;
            if (teamName.length() > 16) {
                teamName = teamName.substring(0, 16);
            }
            
            // Verificar se o time existe
            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                team = scoreboard.registerNewTeam(teamName);
                if (team == null) {
                    System.out.println("[ScoreHelper] ERRO: Não foi possível criar time " + teamName);
                    return;
                }
                
                // Adicionar jogador ao time
                try {
                    team.addPlayer(Bukkit.getOfflinePlayer(entryName));
                } catch (Exception e) {
                    System.out.println("[ScoreHelper] ERRO ao adicionar jogador ao time: " + e.getMessage());
                }
            }
            
            // Verificar comprimento do prefixo e garantir que tenha pelo menos um caractere visível
            String textoFinal = texto;
            if (textoFinal.length() > 16) {
                textoFinal = textoFinal.substring(0, 16);
            }
            
            // Se após truncar não houver caracteres visíveis, usar fallback
            if (ChatColor.stripColor(textoFinal).isEmpty()) {
                textoFinal = "§f...";
            }
            
            // Definir o prefixo do time com o texto
            team.setPrefix(textoFinal);
            
            // Obter o jogador offline para usar como entrada
            OfflinePlayer entry = Bukkit.getOfflinePlayer(entryName);
            
            // Verificar se entry é válido para evitar NullPointerException
            if (entry == null) {
                System.out.println("[ScoreHelper] AVISO: OfflinePlayer é null para " + entryName + " (jogador: " + playerName + ")");
                return; // Não podemos continuar sem uma entrada válida
            }
            
            // Definir o score
            try {
                Score score = objective.getScore(entry);
                if (score.getScore() != valor) {
                    score.setScore(valor);
                }
            } catch (Exception e) {
                System.out.println("[ScoreHelper] ERRO ao definir score: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("[ScoreHelper] ERRO ao atualizar scoreboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gera um nome de entrada único para uma posição específica
     * 
     * @param valor Posição da linha
     * @return Nome da entrada para essa posição
     */
    private String getEntryName(int valor) {
        // Cores como nomes de entrada para garantir unicidade
        String[] cores = {"§1", "§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9", 
                          "§0", "§a", "§b", "§c", "§d", "§e", "§f"};
        
        // Usar modulo para garantir que sempre teremos uma cor válida
        return cores[Math.abs(valor % cores.length)];
    }
    
    /**
     * Limpa todas as linhas da scoreboard sem recriar
     */
    public void limparLinhas() {
        // Em 1.5.2, precisamos ter cuidado com jogadores que saem durante o duelo
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null || !player.isOnline()) {
            System.out.println("[ScoreHelper] Tentativa de limpar linhas para jogador offline: " + playerName);
            return;
        }
        
        // Remover todas as linhas do cache
        Map<Integer, String> linhasDoJogador = linhasAtuais.get(playerName);
        if (linhasDoJogador != null) {
            linhasDoJogador.clear();
        }
        linhasEntradas.clear();
        
        // Remover scores da objective sem recriar a scoreboard
        for (String entry : getEntries()) {
            scoreboard.resetScores(Bukkit.getOfflinePlayer(entry));
        }
    }
    
    /**
     * Obtém todas as entradas da scoreboard
     * 
     * @return Array com os nomes das entradas
     */
    private String[] getEntries() {
        String[] cores = {"§1", "§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9", 
                          "§0", "§a", "§b", "§c", "§d", "§e", "§f"};
        return cores;
    }
    
    /**
     * Define várias linhas de uma vez na scoreboard
     * 
     * @param linhas Lista de textos para as linhas
     */
    public void setLinhas(List<String> linhas) {
        // Em 1.5.2, precisamos ter cuidado com jogadores que saem durante o duelo
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null || !player.isOnline()) {
            System.out.println("[ScoreHelper] Tentativa de atualizar linhas para jogador offline: " + playerName);
            return;
        }
        
        // Limitar a 5 linhas
        int tamanho = Math.min(linhas.size(), 5);
        
        // Verificar quais linhas foram removidas
        Map<Integer, String> linhasDoJogador = linhasAtuais.get(playerName);
        if (linhasDoJogador != null) {
            for (int i = tamanho + 1; i <= 15; i++) {
                if (linhasDoJogador.containsKey(i)) {
                    scoreboard.resetScores(Bukkit.getOfflinePlayer(getEntryName(i)));
                    linhasDoJogador.remove(i);
                }
            }
        }
        
        // Atualizar apenas as linhas que mudaram
        int valor = tamanho;
        for (int i = 0; i < tamanho; i++) {
            setLinha(linhas.get(i), valor--);
        }
    }
    
    /**
     * Atualiza uma linha específica se o conteúdo for diferente
     * 
     * @param valorLinha Posição da linha
     * @param textoNovo Novo texto para a linha
     * @return true se a linha foi atualizada, false se não mudou
     */
    public boolean atualizarLinha(int valorLinha, String textoNovo) {
        // Verificar se a linha é diferente da atual para evitar atualizações desnecessárias
        Map<Integer, String> linhasDoJogador = linhasAtuais.get(playerName);
        if (linhasDoJogador != null && linhasDoJogador.containsKey(valorLinha)) {
            String linhaAtual = linhasDoJogador.get(valorLinha);
            if (linhaAtual.equals(textoNovo)) {
                return false; // Linha não mudou, não precisa atualizar
            }
        }
        
        // Se chegou aqui, a linha mudou e precisa ser atualizada
        setLinha(textoNovo, valorLinha);
        return true;
    }
    
    /**
     * Limpa a scoreboard do jogador
     * 
     * @param player Jogador para limpar a scoreboard
     */
    public static void limparScoreboard(Player player) {
        if (player == null || !player.isOnline()) {
            System.out.println("[ScoreHelper] Tentativa de limpar scoreboard de jogador offline");
            return;
        }
        
        if (helpers.containsKey(player.getName())) {
            helpers.remove(player.getName());
        }
        
        // Limpar cache de linhas
        linhasAtuais.remove(player.getName());
        
        // Definir uma scoreboard vazia para o jogador
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }
    
    /**
     * Remove a scoreboard de todos os jogadores
     */
    public static void limparTodos() {
        for (String playerName : helpers.keySet()) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null && player.isOnline()) {
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
        }
        helpers.clear();
        linhasAtuais.clear();
        truncadosRegistrados.clear();
    }
    
    /**
     * Trunca um nome de jogador de forma inteligente para caber na scoreboard
     * 
     * @param nome Nome original
     * @param tamanhoMaximo Tamanho máximo permitido
     * @return Nome truncado
     */
    public static String truncarNome(String nome, int tamanhoMaximo) {
        if (nome == null || nome.length() <= tamanhoMaximo) {
            return nome;
        }
        
        // Verificar se já temos um apelido registrado para este nome
        String cacheKey = "nome_" + nome;
        if (truncadosRegistrados.containsKey(cacheKey)) {
            return truncadosRegistrados.get(cacheKey);
        }
        
        // Truncar de forma inteligente
        String truncado = nome.substring(0, tamanhoMaximo);
        truncadosRegistrados.put(cacheKey, truncado);
        
        return truncado;
    }

    /**
     * Cria ou atualiza o scoreboard de um jogador
     * 
     * @param player Jogador
     * @param title Título do scoreboard
     * @param lines Linhas do scoreboard
     */
    public void setScoreboard(Player player, String title, List<String> lines) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        try {
            // Obter ou criar scoreboard do jogador
            Scoreboard scoreboard = playerScoreboards.get(player);
            if (scoreboard == null) {
                scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
                playerScoreboards.put(player, scoreboard);
            }
            
            // Obter ou criar objetivo
            Objective objective = playerObjectives.get(player);
            if (objective == null) {
                objective = scoreboard.registerNewObjective("duelo", "dummy");
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                playerObjectives.put(player, objective);
            }
            
            // Limpar scores anteriores
            for (OfflinePlayer entry : scoreboard.getPlayers()) {
                scoreboard.resetScores(entry);
            }
            
            // Definir título
            String safeTitle = messageManager.truncateScoreboardLine(messageManager.sanitizeText(title));
            objective.setDisplayName(safeTitle);
            
            // Adicionar linhas
            if (lines != null && !lines.isEmpty()) {
                int score = lines.size();
                for (String line : lines) {
                    // Sanitizar e truncar a linha
                    String safeLine = messageManager.truncateScoreboardLine(messageManager.sanitizeText(line));
                    
                    // Adicionar a linha ao scoreboard
                    Score scoreObj = objective.getScore(Bukkit.getOfflinePlayer(safeLine));
                    scoreObj.setScore(score--);
                }
            }
            
            // Atualizar scoreboard do jogador
            player.setScoreboard(scoreboard);
            
        } catch (Exception e) {
            mainPlugin.getLogger().severe("Erro ao atualizar scoreboard de " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Remove o scoreboard de um jogador
     * 
     * @param player Jogador
     */
    public void removeScoreboard(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        try {
            // Remover objetivo
            Objective objective = playerObjectives.remove(player);
            if (objective != null) {
                objective.unregister();
            }
            
            // Remover scoreboard
            Scoreboard scoreboard = playerScoreboards.remove(player);
            if (scoreboard != null) {
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
            
        } catch (Exception e) {
            mainPlugin.getLogger().severe("Erro ao remover scoreboard de " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Atualiza uma linha específica do scoreboard
     * 
     * @param player Jogador
     * @param line Nova linha
     * @param score Pontuação da linha
     */
    public void updateLine(Player player, String line, int score) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        try {
            Scoreboard scoreboard = playerScoreboards.get(player);
            Objective objective = playerObjectives.get(player);
            
            if (scoreboard != null && objective != null) {
                // Sanitizar e truncar a linha
                String safeLine = messageManager.truncateScoreboardLine(messageManager.sanitizeText(line));
                
                // Atualizar a linha
                Score scoreObj = objective.getScore(Bukkit.getOfflinePlayer(safeLine));
                scoreObj.setScore(score);
            }
            
        } catch (Exception e) {
            mainPlugin.getLogger().severe("Erro ao atualizar linha do scoreboard de " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Limpa todas as linhas do scoreboard
     * 
     * @param player Jogador
     */
    public void clearLines(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        try {
            Scoreboard scoreboard = playerScoreboards.get(player);
            if (scoreboard != null) {
                for (OfflinePlayer entry : scoreboard.getPlayers()) {
                    scoreboard.resetScores(entry);
                }
            }
            
        } catch (Exception e) {
            mainPlugin.getLogger().severe("Erro ao limpar linhas do scoreboard de " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Verifica se um jogador tem um scoreboard ativo
     * 
     * @param player Jogador
     * @return true se o jogador tem um scoreboard
     */
    public boolean hasScoreboard(Player player) {
        return player != null && playerScoreboards.containsKey(player);
    }
    
    /**
     * Obtém o scoreboard de um jogador
     * 
     * @param player Jogador
     * @return Scoreboard do jogador
     */
    public Scoreboard getScoreboard(Player player) {
        return playerScoreboards.get(player);
    }
    
    /**
     * Obtém o objetivo do scoreboard de um jogador
     * 
     * @param player Jogador
     * @return Objetivo do scoreboard
     */
    public Objective getObjective(Player player) {
        return playerObjectives.get(player);
    }
    
    /**
     * Limpa todos os scoreboards
     */
    public void clearAll() {
        for (Player player : new ArrayList<>(playerScoreboards.keySet())) {
            removeScoreboard(player);
        }
    }
} 