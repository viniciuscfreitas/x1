package br.com.primeleague.x1.enums;

/**
 * Tipos de duelo disponíveis
 */
public enum DuelType {
    
    // Duelos tradicionais
    ARENA("Arena", true, true, "Duelo em Arena"),
    LOCAL("Local", false, false, "Duelo no Local"),
    
    // Duelos com kits específicos
    ARENA_KIT("Arena com Kit", true, true, "Kit Arena"),
    LOCAL_KIT("Local com Kit", false, true, "Kit Local"),
    
    // Duelos em equipe 2x2
    TEAM_2X2_ARENA("2x2 Arena", true, true, "2x2 Arena"),
    TEAM_2X2_LOCAL("2x2 Local", false, true, "2x2 Local"),
    
    // Duelos em equipe 3x3
    TEAM_3X3_ARENA("3x3 Arena", true, true, "3x3 Arena"),
    TEAM_3X3_LOCAL("3x3 Local", false, true, "3x3 Local");
    
    private final String name;
    private final boolean arena;
    private final boolean usesKit;
    private final String formattedName;
    
    /**
     * Construtor
     * 
     * @param name Nome do tipo
     * @param arena Se é um duelo em arena
     * @param usesKit Se usa kit
     * @param formattedName Nome formatado para exibição
     */
    DuelType(String name, boolean arena, boolean usesKit, String formattedName) {
        this.name = name;
        this.arena = arena;
        this.usesKit = usesKit;
        this.formattedName = formattedName;
    }
    
    /**
     * Obtém o nome do tipo
     * 
     * @return Nome do tipo
     */
    public String getName() {
        return name;
    }
    
    /**
     * Verifica se é um duelo em arena
     * 
     * @return true se for em arena, false caso contrário
     */
    public boolean isArena() {
        return arena;
    }
    
    /**
     * Verifica se é um duelo local
     * 
     * @return true se for local, false caso contrário
     */
    public boolean isLocal() {
        return !arena;
    }
    
    /**
     * Verifica se usa kit
     * 
     * @return true se usar kit, false caso contrário
     */
    public boolean usesKit() {
        return usesKit;
    }
    
    /**
     * Obtém o nome formatado para exibição
     * 
     * @return Nome formatado
     */
    public String getFormattedName() {
        return formattedName;
    }
    
    /**
     * Verifica se é um duelo em equipe
     * 
     * @return true se for um duelo em equipe, false caso contrário
     */
    public boolean isTeamDuel() {
        return this == TEAM_2X2_ARENA || this == TEAM_2X2_LOCAL || 
               this == TEAM_3X3_ARENA || this == TEAM_3X3_LOCAL;
    }
    
    /**
     * Verifica se é um duelo 2x2
     * 
     * @return true se for um duelo 2x2, false caso contrário
     */
    public boolean is2x2() {
        return this == TEAM_2X2_ARENA || this == TEAM_2X2_LOCAL;
    }
    
    /**
     * Verifica se é um duelo 3x3
     * 
     * @return true se for um duelo 3x3, false caso contrário
     */
    public boolean is3x3() {
        return this == TEAM_3X3_ARENA || this == TEAM_3X3_LOCAL;
    }
    
    /**
     * Obtém o tamanho da equipe para este tipo de duelo
     * 
     * @return 2 para duelos 2x2, 3 para duelos 3x3, 1 para duelos individuais
     */
    public int getTeamSize() {
        if (is2x2()) {
            return 2;
        } else if (is3x3()) {
            return 3;
        } else {
            return 1;
        }
    }
} 