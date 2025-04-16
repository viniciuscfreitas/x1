package br.com.primeleague.x1.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;

import br.com.primeleague.x1.Main;
import br.com.primeleague.x1.enums.DuelState;
import br.com.primeleague.x1.enums.X1HUDState;
import br.com.primeleague.x1.models.Duel;
import br.com.primeleague.x1.rival.RivalData;
import br.com.primeleague.x1.utils.ColorUtils;
import br.com.primeleague.x1.utils.ScoreHelper;

/**
 * Gerenciador de Scoreboards para o sistema de X1
 * Responsável por exibir e atualizar informações visuais durante os duelos
 */
public class X1ScoreboardManager {

    private static Main plugin;
    private static Map<String, X1HUDState> playerHudStates;
    private static Map<String, BukkitTask> playerTasks;
    private static Map<String, Map<String, Object>> playerStats;
    private static Map<String, Scoreboard> scoreboardsAtivos;
    private static boolean enabled = true;
    
    // Constantes para evitar números mágicos
    private static final int UPDATE_INTERVAL = 20; // 1 segundo
    private static final int POST_DUEL_DURATION = 3; // 3 segundos

    /**
     * Inicializa o gerenciador de scoreboards
     * 
     * @param mainPlugin Instância do plugin principal
     */
    public static void initialize(Main mainPlugin) {
        plugin = mainPlugin;
        playerHudStates = new HashMap<String, X1HUDState>();
        playerTasks = new HashMap<String, BukkitTask>();
        playerStats = new HashMap<String, Map<String, Object>>();
        scoreboardsAtivos = new HashMap<String, Scoreboard>();
        
        // Inicializar o ScoreHelper com a referência do plugin
        ScoreHelper.initialize(mainPlugin);
        
        // Verificar se o sistema está habilitado na configuração
        enabled = plugin.getConfigManager().getMessagesConfig().getBoolean("scoreboard.enabled", true);
        
        if (enabled) {
            plugin.getLogger().info("Sistema de Scoreboard X1 ativado com sucesso");
        } else {
            plugin.getLogger().info("Sistema de Scoreboard X1 desativado nas configurações");
        }
    }
    
    /**
     * Verifica se o sistema de scoreboards está habilitado
     * 
     * @return true se o sistema estiver habilitado
     */
    public static boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Exibe a scoreboard para o estado de pré-duelo (contagem regressiva)
     * 
     * @param player Jogador para exibir a scoreboard
     * @param duel Duelo atual
     */
    public static void mostrarPreDuel(final Player player, final Duel duel) {
        if (!enabled || player == null || !player.isOnline() || duel == null) {
            if (player != null) {
                System.out.println("[X1ScoreboardManager] Não foi possível mostrar HUD pré-duelo para: " + player.getName());
            }
            return;
        }
        
        // Cancelar qualquer tarefa existente para este jogador
        cancelarTarefaAtual(player.getName());
        
        // Definir estado atual do jogador
        playerHudStates.put(player.getName(), X1HUDState.PRE_DUEL);
        
        // Inicializar estatísticas do jogador
        inicializarEstatsJogador(player.getName(), duel);
        
        // Atualizar scoreboard imediatamente
        atualizarScoreboardPreDuel(player, duel);
        
        // Criar tarefa de atualização da scoreboard
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                // Verificar se o jogador ainda está online
                if (player == null || !player.isOnline()) {
                    this.cancel();
                    playerTasks.remove(player.getName());
                    playerHudStates.remove(player.getName());
                    return;
                }
                
                // Verificar se o jogador ainda está em duelo
                if (!plugin.getDuelManager().isInDuel(player.getName())) {
                    limpar(player);
                    this.cancel();
                    return;
                }
                
                // Verificar se o duelo mudou de estado
                if (duel.getState() == DuelState.IN_PROGRESS) {
                    mostrarDuel(player, duel);
                    this.cancel();
                    return;
                }
                
                atualizarScoreboardPreDuel(player, duel);
            }
        }.runTaskTimer(plugin, UPDATE_INTERVAL, UPDATE_INTERVAL); // Atualiza a cada 1 segundo
        
        // Armazenar a tarefa
        playerTasks.put(player.getName(), task);
    }
    
    /**
     * Exibe a scoreboard para o estado de duelo em andamento
     * 
     * @param player Jogador para exibir a scoreboard
     * @param duel Duelo atual
     */
    public static void mostrarDuel(final Player player, final Duel duel) {
        if (!enabled || player == null || !player.isOnline() || duel == null) {
            if (player != null) {
                System.out.println("[X1ScoreboardManager] Não foi possível mostrar HUD duelo para: " + player.getName());
            }
            return;
        }
        
        // Cancelar qualquer tarefa existente para este jogador
        cancelarTarefaAtual(player.getName());
        
        // Definir estado atual do jogador
        playerHudStates.put(player.getName(), X1HUDState.IN_DUEL);
        
        // Atualizar scoreboard imediatamente
        if (duel.isTeamDuel()) {
            atualizarScoreboardTeamDuel(player, duel);
        } else {
            atualizarScoreboardDuel(player, duel);
        }
        
        // Criar tarefa de atualização da scoreboard
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                // Verificar se o jogador ainda está online
                if (player == null || !player.isOnline()) {
                    this.cancel();
                    playerTasks.remove(player.getName());
                    playerHudStates.remove(player.getName());
                    return;
                }
                
                // Verificar se o jogador ainda está em duelo
                if (!plugin.getDuelManager().isInDuel(player.getName())) {
                    limpar(player);
                    this.cancel();
                    return;
                }
                
                // Verificar se o duelo mudou de estado
                if (duel.getState() == DuelState.ENDED) {
                    this.cancel();
                    return;
                }
                
                if (duel.isTeamDuel()) {
                    atualizarScoreboardTeamDuel(player, duel);
                } else {
                    atualizarScoreboardDuel(player, duel);
                }
            }
        }.runTaskTimer(plugin, UPDATE_INTERVAL, UPDATE_INTERVAL); // Atualiza a cada 1 segundo
        
        // Armazenar a tarefa
        playerTasks.put(player.getName(), task);
    }
    
    /**
     * Exibe a scoreboard para o estado de pós-duelo (resultado)
     * 
     * @param player Jogador para exibir a scoreboard
     * @param duel Duelo finalizado
     * @param venceu true se o jogador venceu o duelo
     */
    public static void mostrarResultado(final Player player, final Duel duel, final boolean venceu) {
        if (!enabled || player == null || !player.isOnline() || duel == null) {
            if (player != null) {
                System.out.println("[X1ScoreboardManager] Não foi possível mostrar HUD resultado para: " + player.getName());
            }
            return;
        }
        
        // Cancelar qualquer tarefa existente para este jogador
        cancelarTarefaAtual(player.getName());
        
        // Definir estado atual do jogador
        playerHudStates.put(player.getName(), X1HUDState.FINAL_STATS);
        
        // Atualizar scoreboard imediatamente
        atualizarScoreboardFinalStats(player, duel, venceu);
        
        // Criar tarefa para remover a scoreboard após 3 segundos
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                // Verificar se o player ainda está online
                if (player == null || !player.isOnline()) {
                    System.out.println("[X1ScoreboardManager] Jogador offline ao finalizar HUD: " + 
                                     (player != null ? player.getName() : "null"));
                    return;
                }
                
                limpar(player);
                
                // Restaurar HUD Global (do plugin PrimeLeagueDisplay)
                try {
                    // Acessar o método estático da classe HUDManager do plugin PrimeLeagueDisplay
                    Class<?> hudManagerClass = Class.forName("br.com.primeleague.display.HUDManager");
                    java.lang.reflect.Method method = hudManagerClass.getMethod("restaurarHUDPadrao", Player.class);
                    method.invoke(null, player);
                } catch (Exception e) {
                    // Caso o método não seja encontrado, não fazer nada
                    plugin.getLogger().fine("HUDManager.restaurarHUDPadrao não encontrado: " + e.getMessage());
                }
            }
        }.runTaskLater(plugin, POST_DUEL_DURATION * 20L); // 3 segundos
        
        // Armazenar a tarefa
        playerTasks.put(player.getName(), task);
    }
    
    /**
     * Limpa a scoreboard do jogador
     * 
     * @param player Jogador para limpar a scoreboard
     */
    public static void limpar(Player player) {
        if (player == null || !player.isOnline()) {
            System.out.println("[X1ScoreboardManager] Tentativa de limpar HUD de jogador offline.");
            return;
        }
        
        // Cancelar tarefas de atualização
        cancelarTarefaAtual(player.getName());
        
        // Remover scoreboard
        ScoreHelper.limparScoreboard(player);
        
        // Remover do mapa de scoreboards ativos
        scoreboardsAtivos.remove(player.getName());
        
        // Limpar estatísticas
        playerStats.remove(player.getName());
        
        // Atualizar estado
        playerHudStates.put(player.getName(), X1HUDState.NONE);
    }
    
    /**
     * Atualiza a scoreboard no estado de pré-duelo
     * 
     * @param player Jogador para atualizar a scoreboard
     * @param duel Duelo atual
     */
    private static void atualizarScoreboardPreDuel(Player player, Duel duel) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        // Obter título configurado
        String titulo = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.preduel.title", "&eAguardando duelo");
        
        // Obter scoreboard do jogador
        ScoreHelper helper = ScoreHelper.getHelper(player, titulo);
        if (helper == null) {
            return; // Jogador pode ter saído durante a execução
        }
        
        // Preparar linhas
        List<String> linhas = new ArrayList<String>();
        
        // Adicionar linha do modo
        String modoTexto = duel.getType().isArena() ? "Arena" : "Local";
        String modo = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.preduel.mode", "&fModo: &a{modo}");
        modo = modo.replace("{modo}", modoTexto);
        linhas.add(ColorUtils.colorize(modo));
        
        // Adicionar linha da contagem regressiva
        int countdown = duel.getCountdown(); // Obter contagem regressiva do duelo
        if (countdown < 0) countdown = 5; // Valor padrão se não estiver disponível
        
        String tempo = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.preduel.time", "&fInício: &c{tempo}s");
        tempo = tempo.replace("{tempo}", String.valueOf(countdown));
        linhas.add(ColorUtils.colorize(tempo));
        
        // Adicionar informações do oponente/equipe
        if (duel.isTeamDuel()) {
            // Para duelos em equipe, mostrar contagem de jogadores
            String textoTime1 = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.preduel.team1", "&9Azul: &f{qtd1}");
            textoTime1 = textoTime1.replace("{qtd1}", String.valueOf(duel.getTeam1().size()));
            linhas.add(ColorUtils.colorize(textoTime1));
            
            String textoTime2 = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.preduel.team2", "&cVerm.: &f{qtd2}");
            textoTime2 = textoTime2.replace("{qtd2}", String.valueOf(duel.getTeam2().size()));
            linhas.add(ColorUtils.colorize(textoTime2));
        } else {
            // Para duelos 1v1, mostrar nome do oponente
            String oponente = player.getName().equals(duel.getPlayer1()) ? duel.getPlayer2() : duel.getPlayer1();
            
            // Truncar nome se necessário
            String oponenteTruncado = ScoreHelper.truncarNome(oponente, 10); // Deixa espaço para "Contra: "
            
            String textoOponente = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.preduel.enemy", "&fContra: &b{jogador}");
            textoOponente = textoOponente.replace("{jogador}", oponenteTruncado);
            linhas.add(ColorUtils.colorize(textoOponente));
        }
        
        // Atualizar linhas na scoreboard
        helper.setLinhas(linhas);
    }
    
    /**
     * Atualiza a scoreboard no estado de duelo 1v1
     * 
     * @param player Jogador para atualizar a scoreboard
     * @param duel Duelo atual
     */
    private static void atualizarScoreboardDuel(Player player, Duel duel) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        // Obter título configurado
        String titulo = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.duel.title", "&6X1 em andamento");
        
        // Obter scoreboard do jogador
        ScoreHelper helper = ScoreHelper.getHelper(player, titulo);
        if (helper == null) {
            return; // Jogador pode ter saído durante a execução
        }
        
        // Preparar linhas
        List<String> linhas = new ArrayList<String>();
        
        // Adicionar linha de vida do jogador
        String vidaJogador = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.duel.you", "&aVocê: &f{vida}");
        vidaJogador = vidaJogador.replace("{vida}", String.valueOf((int)player.getHealth()) + "❤");
        linhas.add(ColorUtils.colorize(vidaJogador));
        
        // Adicionar linha de vida do oponente
        String oponente = player.getName().equals(duel.getPlayer1()) ? duel.getPlayer2() : duel.getPlayer1();
        
        // Truncar nome se necessário
        String oponenteTruncado = ScoreHelper.truncarNome(oponente, 10); // Deixa espaço para "Vs: "
        
        Player oponentePlayer = Bukkit.getPlayerExact(oponente);
        String vidaOponente = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.duel.vs", "&cVs: &f{vida}");
        vidaOponente = vidaOponente.replace("{vida}", 
            oponentePlayer != null && oponentePlayer.isOnline() ? String.valueOf((int)oponentePlayer.getHealth()) + "❤" : "?❤");
        linhas.add(ColorUtils.colorize(vidaOponente));
        
        // Verificar se existe uma rivalidade entre os jogadores
        boolean isRivalry = plugin.getRivalManager().isRivalry(player.getName(), oponente);
        if (isRivalry && plugin.getConfigManager().getConfig().getBoolean("rivalidade.mostrar_scoreboard", true)) {
            // Obter dados da rivalidade
            RivalData rivalData = plugin.getRivalManager().getRivalData(player.getName(), oponente);
            if (rivalData != null) {
                int p1victories = rivalData.getVictories(player.getName());
                int p2victories = rivalData.getVictories(oponente);
                
                // Linha de placar de rivalidade
                String rivalPlacar = plugin.getMessageManager().getMessage("rival.placar");
                rivalPlacar = rivalPlacar.replace("{vitorias1}", String.valueOf(p1victories))
                                        .replace("{vitorias2}", String.valueOf(p2victories));
                linhas.add(ColorUtils.colorize(rivalPlacar));
            }
        }
        
        // Adicionar linha de tempo
        int duracao = duel.getDuration();
        String tempoFormatado = String.format("%02d:%02d", duracao / 60, duracao % 60);
        String textoTempo = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.duel.time", "&fTempo: &e{tempo}");
        textoTempo = textoTempo.replace("{tempo}", tempoFormatado);
        linhas.add(ColorUtils.colorize(textoTempo));
        
        // Adicionar linha de aposta (se houver)
        double aposta = duel.getBetAmount();
        if (aposta > 0) {
            String textoAposta = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.duel.bet", "&6Aposta: &f${valor}");
            textoAposta = textoAposta.replace("{valor}", String.valueOf((int)aposta));
            linhas.add(ColorUtils.colorize(textoAposta));
        }
        
        // Atualizar linhas na scoreboard
        helper.setLinhas(linhas);
    }
    
    /**
     * Atualiza a scoreboard no estado de duelo em equipe
     * 
     * @param player Jogador para atualizar a scoreboard
     * @param duel Duelo atual
     */
    private static void atualizarScoreboardTeamDuel(Player player, Duel duel) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        // Obter título configurado
        String titulo = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.team.title", "&6Duelo de Times");
        
        // Obter scoreboard do jogador
        ScoreHelper helper = ScoreHelper.getHelper(player, titulo);
        if (helper == null) {
            return; // Jogador pode ter saído durante a execução
        }
        
        // Preparar linhas
        List<String> linhas = new ArrayList<String>();
        
        // Adicionar linha da equipe azul
        int vivos1 = contarJogadoresVivos(duel.getTeam1());
        String textoTime1 = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.team.blue", "&9Azul: &f{v1}");
        textoTime1 = textoTime1.replace("{v1}", String.valueOf(vivos1));
        linhas.add(ColorUtils.colorize(textoTime1));
        
        // Adicionar linha da equipe vermelha
        int vivos2 = contarJogadoresVivos(duel.getTeam2());
        String textoTime2 = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.team.red", "&cVerm.: &f{v2}");
        textoTime2 = textoTime2.replace("{v2}", String.valueOf(vivos2));
        linhas.add(ColorUtils.colorize(textoTime2));
        
        // Adicionar linha de tempo
        int duracao = duel.getDuration();
        String tempoFormatado = String.format("%02d:%02d", duracao / 60, duracao % 60);
        String textoTempo = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.team.time", "&fTempo: &e{tempo}");
        textoTempo = textoTempo.replace("{tempo}", tempoFormatado);
        linhas.add(ColorUtils.colorize(textoTempo));
        
        // Adicionar linha de aposta (se houver)
        double aposta = duel.getBetAmount();
        if (aposta > 0) {
            String textoAposta = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.team.bet", "&6Aposta: &f${valor}");
            textoAposta = textoAposta.replace("{valor}", String.valueOf((int)aposta));
            linhas.add(ColorUtils.colorize(textoAposta));
        }
        
        // Atualizar linhas na scoreboard
        helper.setLinhas(linhas);
    }
    
    /**
     * Atualiza a scoreboard de estatísticas finais do duelo
     * 
     * @param player Jogador para atualizar a scoreboard
     * @param duel Duelo finalizado
     * @param venceu true se o jogador venceu o duelo
     */
    private static void atualizarScoreboardFinalStats(Player player, Duel duel, boolean venceu) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        // Obter título configurado
        String titulo = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.end.title", "&aDuelo Finalizado");
        
        // Obter scoreboard do jogador
        ScoreHelper helper = ScoreHelper.getHelper(player, titulo);
        if (helper == null) {
            return; // Jogador pode ter saído durante a execução
        }
        
        // Preparar linhas
        List<String> linhas = new ArrayList<String>();
        
        // Adicionar linha do vencedor
        String vencedor = venceu ? player.getName() : (duel.getWinner() != null ? duel.getWinner() : "Empate");
        String vencedorTruncado = ScoreHelper.truncarNome(vencedor, 10); // Deixa espaço para a formatação
        
        String textoVencedor = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.end.winner", "&aVitória de: &b{v}");
        textoVencedor = textoVencedor.replace("{v}", vencedorTruncado);
        linhas.add(ColorUtils.colorize(textoVencedor));
        
        // Adicionar linha do perdedor
        if (!duel.isDraw()) {
            String perdedor = !venceu ? player.getName() : 
                (player.getName().equals(duel.getPlayer1()) ? duel.getPlayer2() : duel.getPlayer1());
            String perdedorTruncado = ScoreHelper.truncarNome(perdedor, 10); // Deixa espaço para a formatação
            
            String textoPerdedor = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.end.loser", "&cDerrota de: &7{p}");
            textoPerdedor = textoPerdedor.replace("{p}", perdedorTruncado);
            linhas.add(ColorUtils.colorize(textoPerdedor));
        }
        
        // Obter dano causado e recebido do mapa de estatísticas
        int danoCausado = 0;
        int danoRecebido = 0;
        
        if (playerStats.containsKey(player.getName())) {
            Map<String, Object> stats = playerStats.get(player.getName());
            if (stats.containsKey("damageDealt")) {
                danoCausado = ((Number) stats.get("damageDealt")).intValue();
            }
            if (stats.containsKey("damageTaken")) {
                danoRecebido = ((Number) stats.get("damageTaken")).intValue();
            }
        }
        
        // Adicionar linha de dano causado
        String textoDano = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.end.damage", "&eDano Causado: &f{dc}");
        textoDano = textoDano.replace("{dc}", String.valueOf(danoCausado));
        linhas.add(ColorUtils.colorize(textoDano));
        
        // Adicionar linha de dano recebido
        String textoDanoRecebido = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.end.damageTaken", "&eRecebido: &f{dr}");
        textoDanoRecebido = textoDanoRecebido.replace("{dr}", String.valueOf(danoRecebido));
        linhas.add(ColorUtils.colorize(textoDanoRecebido));
        
        // Adicionar streak do jogador
        int streak = plugin.getStatsManager().getPlayerStats(player.getName()).getStreak();
        String textoStreak = plugin.getConfigManager().getMessagesConfig().getString("scoreboard.end.streak", "&6Streak: &f{streak}");
        textoStreak = textoStreak.replace("{streak}", String.valueOf(streak));
        linhas.add(ColorUtils.colorize(textoStreak));
        
        // Atualizar linhas na scoreboard
        helper.setLinhas(linhas);
    }
    
    /**
     * Registra o dano causado por um jogador
     * 
     * @param playerName Nome do jogador que causou dano
     * @param amount Quantidade de dano causado
     */
    public static void registrarDano(String playerName, double amount) {
        if (!playerStats.containsKey(playerName)) {
            return;
        }
        
        Map<String, Object> stats = playerStats.get(playerName);
        int danoCausado = stats.containsKey("damageDealt") ? ((Number) stats.get("damageDealt")).intValue() : 0;
        stats.put("damageDealt", danoCausado + (int)amount);
    }
    
    /**
     * Registra o dano recebido por um jogador
     * 
     * @param playerName Nome do jogador que recebeu dano
     * @param amount Quantidade de dano recebido
     */
    public static void registrarDanoRecebido(String playerName, double amount) {
        if (!playerStats.containsKey(playerName)) {
            return;
        }
        
        Map<String, Object> stats = playerStats.get(playerName);
        int danoRecebido = stats.containsKey("damageTaken") ? ((Number) stats.get("damageTaken")).intValue() : 0;
        stats.put("damageTaken", danoRecebido + (int)amount);
    }
    
    /**
     * Conta o número de jogadores vivos em uma equipe
     * 
     * @param team Lista de jogadores da equipe
     * @return Número de jogadores vivos
     */
    private static int contarJogadoresVivos(List<String> team) {
        int vivos = 0;
        
        for (String playerName : team) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null && player.isOnline() && !player.isDead()) {
                vivos++;
            }
        }
        
        return vivos;
    }
    
    /**
     * Cancela a tarefa atual de atualização da scoreboard para um jogador
     * 
     * @param playerName Nome do jogador
     */
    private static void cancelarTarefaAtual(String playerName) {
        if (playerTasks.containsKey(playerName)) {
            BukkitTask task = playerTasks.get(playerName);
            if (task != null) {
                task.cancel();
            }
            playerTasks.remove(playerName);
        }
    }
    
    /**
     * Obtém o estado atual da HUD de um jogador
     * 
     * @param playerName Nome do jogador
     * @return Estado atual da HUD
     */
    public static X1HUDState getHUDState(String playerName) {
        return playerHudStates.getOrDefault(playerName, X1HUDState.NONE);
    }
    
    /**
     * Limpa todos os recursos e tarefas
     * Chamado quando o plugin é desabilitado
     */
    public static void desabilitar() {
        // Cancelar todas as tarefas pendentes
        for (BukkitTask task : playerTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        
        // Limpar todas as scoreboards
        for (Player player : Bukkit.getOnlinePlayers()) {
            ScoreHelper.limparScoreboard(player);
        }
        
        // Limpar listas
        playerTasks.clear();
        playerHudStates.clear();
        playerStats.clear();
        scoreboardsAtivos.clear();
    }
    
    /**
     * Inicializa o mapa de estatísticas para um jogador
     * 
     * @param playerName Nome do jogador
     * @param duel Duelo atual
     */
    private static void inicializarEstatsJogador(String playerName, Duel duel) {
        Map<String, Object> stats = new HashMap<String, Object>();
        stats.put("damageDealt", 0);
        stats.put("damageTaken", 0);
        stats.put("startTime", System.currentTimeMillis());
        playerStats.put(playerName, stats);
    }
} 