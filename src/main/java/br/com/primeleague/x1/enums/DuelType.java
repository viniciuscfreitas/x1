package br.com.primeleague.x1.enums;

/**
 * Tipos possíveis de duelo
 */
public enum DuelType {
    
    /**
     * Duelo na arena configurada (com itens do próprio jogador)
     */
    ARENA("Arena"),
    
    /**
     * Duelo local (com itens do próprio jogador)
     */
    LOCAL("Local"),
    
    /**
     * Duelo na arena com kit configurado
     */
    ARENA_KIT("Arena com Kit"),
    
    /**
     * Duelo local com kit configurado
     */
    LOCAL_KIT("Local com Kit"),
    
    /**
     * Duelo em equipe
     */
    TEAM("Equipe");
    
    private final String formattedName;
    
    /**
     * Construtor
     * 
     * @param formattedName Nome formatado para exibição
     */
    DuelType(String formattedName) {
        this.formattedName = formattedName;
    }
    
    /**
     * Obtém o nome formatado do tipo de duelo
     * 
     * @return Nome formatado
     */
    public String getFormattedName() {
        return formattedName;
    }
    
    /**
     * Verifica se o tipo de duelo usa kit configurado
     * 
     * @return true se usar kit configurado, false caso contrário
     */
    public boolean usesKit() {
        return this == ARENA_KIT || this == LOCAL_KIT;
    }
    
    /**
     * Verifica se o tipo de duelo acontece na arena
     * 
     * @return true se for na arena, false caso contrário
     */
    public boolean isArena() {
        return this == ARENA || this == ARENA_KIT;
    }
    
    /**
     * Verifica se o tipo de duelo é local
     * 
     * @return true se for local, false caso contrário
     */
    public boolean isLocal() {
        return this == LOCAL || this == LOCAL_KIT;
    }
} 