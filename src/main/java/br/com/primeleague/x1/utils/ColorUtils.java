package br.com.primeleague.x1.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

/**
 * Utilitários para processamento de cores em mensagens
 */
public class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    /**
     * Aplica cores a uma string
     * 
     * @param text Texto a ser colorido
     * @return Texto colorido
     */
    public static String colorize(String text) {
        if (text == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    
    /**
     * Aplica cores a uma lista de strings
     * 
     * @param textList Lista de textos a serem coloridos
     * @return Lista de textos coloridos
     */
    public static List<String> colorize(List<String> textList) {
        List<String> colorized = new ArrayList<>();
        
        for (String text : textList) {
            colorized.add(colorize(text));
        }
        
        return colorized;
    }
    
    /**
     * Traduz um código de cor hexadecimal para o formato do Bukkit
     * 
     * @param hexColor Código de cor hexadecimal
     * @return Código de cor no formato do Bukkit
     */
    private static String translateHexColorCodes(String hexColor) {
        StringBuilder builder = new StringBuilder("§x");
        
        for (char c : hexColor.toCharArray()) {
            builder.append("§").append(c);
        }
        
        return builder.toString();
    }
    
    /**
     * Remove todas as cores de uma string
     * 
     * @param text Texto para remover as cores
     * @return Texto sem cores
     */
    public static String stripColor(String text) {
        if (text == null) {
            return "";
        }
        return ChatColor.stripColor(text);
    }
    
    /**
     * Limita o tamanho de uma string colorida, preservando os códigos de cor
     * e garantindo que o texto visível não exceda o limite de caracteres
     * 
     * @param text Texto colorido
     * @param limit Limite de caracteres
     * @return Texto colorido limitado
     */
    public static String limitColorizedText(String text, int limit) {
        if (text == null || text.isEmpty() || text.length() <= limit) {
            return text;
        }
        
        String stripped = ChatColor.stripColor(text);
        
        if (stripped.length() <= limit) {
            return text;
        }
        
        // Ajustar o limite para considerar os códigos de cor
        int colorCodes = text.length() - stripped.length();
        int adjustedLimit = limit + colorCodes;
        
        if (text.length() <= adjustedLimit) {
            return text;
        }
        
        // Contar os códigos de cor até o limite ajustado
        int codeCount = 0;
        for (int i = 0; i < Math.min(text.length(), adjustedLimit); i++) {
            if (text.charAt(i) == '§' && i + 1 < text.length()) {
                codeCount += 2;
                i++; // Pular o próximo caractere
            }
        }
        
        // Calcular o limite final com base nos códigos de cor encontrados
        int finalLimit = limit + codeCount;
        
        // Verificar se é necessário adicionar ".." ao final
        if (text.length() > finalLimit) {
            // Se o texto acabar em um código de cor, ajustar
            if (finalLimit - 2 >= 0 && text.charAt(finalLimit - 2) == '§') {
                finalLimit -= 2;
            }
            
            // Garantir que o texto final tenha espaço para ".."
            String result = text.substring(0, Math.max(0, finalLimit - 2)) + "..";
            
            // Preservar a última cor encontrada
            String lastColor = getLastColor(text.substring(0, Math.max(0, finalLimit - 2)));
            return lastColor + result;
        }
        
        return text.substring(0, finalLimit);
    }
    
    /**
     * Obtém o último código de cor encontrado em uma string
     * 
     * @param text Texto a ser analisado
     * @return Último código de cor
     */
    private static String getLastColor(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        String lastColor = "";
        boolean foundColor = false;
        
        for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == '§') {
                foundColor = true;
                char code = text.charAt(i + 1);
                
                // Se for um código de reset, limpar cores anteriores
                if (code == 'r') {
                    lastColor = "§r";
                } else {
                    // Se for um código de formatação, adicionar ao último código
                    if ("lmno".indexOf(code) != -1) {
                        if (!lastColor.contains("" + code)) {
                            lastColor += "§" + code;
                        }
                    } else {
                        // Se for um código de cor, substituir a cor anterior
                        String colorCodes = "0123456789abcdef";
                        if (colorCodes.indexOf(code) != -1) {
                            // Remover códigos de cor anteriores
                            lastColor = lastColor.replaceAll("§[0-9a-f]", "");
                            lastColor += "§" + code;
                        }
                    }
                }
                
                i++; // Pular o próximo caractere
            }
        }
        
        return foundColor ? lastColor : "";
    }
    
    /**
     * Aplica cores em todas as strings de uma lista
     * 
     * @param lines Lista de strings para colorir
     * @return Lista de strings coloridas
     */
    public static String[] colorize(String[] lines) {
        if (lines == null) {
            return new String[0];
        }
        
        String[] colorized = new String[lines.length];
        for (int i = 0; i < lines.length; i++) {
            colorized[i] = colorize(lines[i]);
        }
        return colorized;
    }
} 