package br.com.primeleague.x1.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.file.Files;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

/**
 * Utilitários para manipulação de arquivos
 * 
 * @author Vinícius Henrique
 */
public class FileUtils {

    /**
     * Carrega um arquivo YAML com codificação UTF-8 explícita
     * 
     * @param file O arquivo a ser carregado
     * @return A configuração YAML carregada
     */
    public static YamlConfiguration loadUTF8YamlConfiguration(File file) {
        try {
            if (!file.exists()) {
                return new YamlConfiguration();
            }
            
            // Usar UTF-8 explicitamente
            FileInputStream fileInputStream = new FileInputStream(file);
            
            // Carregamento manual
            YamlConfiguration config = YamlConfiguration.loadConfiguration(fileInputStream);
            fileInputStream.close();
            
            return config;
        } catch (Exception e) {
            // Em caso de erro, retornar uma configuração vazia e logar o erro
            System.err.println("Erro ao carregar arquivo YAML: " + file.getName());
            e.printStackTrace();
            return new YamlConfiguration();
        }
    }
    
    /**
     * Verifica se um arquivo contém caracteres Unicode que podem causar problemas
     * 
     * @param file O arquivo a verificar
     * @return true se encontrar caracteres problemáticos
     */
    public static boolean containsProblematicUnicodeCharacters(File file) {
        try {
            byte[] bytes = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(bytes);
            fis.close();
            
            // Converter para string para verificar
            String content = new String(bytes, "UTF-8");
            
            // Verificar emojis e outros caracteres problemáticos (fora do conjunto ASCII ou Latin-1)
            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c > 255) { // Caracteres fora do Latin-1
                    // Pular '\n', '\r' e '\t'
                    if (c != '\n' && c != '\r' && c != '\t') {
                        return true;
                    }
                }
            }
            
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Remove caracteres Unicode problemáticos de um arquivo
     * 
     * @param file O arquivo a ser processado
     * @return true se o arquivo foi processado com sucesso
     */
    public static boolean removeUnicodeCharacters(File file) {
        try {
            // Ler o arquivo
            StringBuilder content = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            
            while ((line = reader.readLine()) != null) {
                // Processar cada caractere
                StringBuilder cleanLine = new StringBuilder();
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (c <= 255 || c == '\n' || c == '\r' || c == '\t') {
                        cleanLine.append(c);
                    } else {
                        // Remover o caractere ou substituir por algo seguro
                        // Podemos deixar vazio ou substituir por um placeholder
                    }
                }
                
                content.append(cleanLine).append("\n");
            }
            
            reader.close();
            
            // Gravar o conteúdo processado de volta no arquivo
            FileWriter writer = new FileWriter(file);
            writer.write(content.toString());
            writer.close();
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Calcula o hash MD5 de um InputStream
     *
     * @param input InputStream a ser processado
     * @return String contendo o hash MD5 em hexadecimal
     * @throws IOException em caso de erro de leitura
     * @throws NoSuchAlgorithmException se o algoritmo MD5 não estiver disponível
     */
    public static String calculateMD5(InputStream input) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[8192];
        int read;
        
        while ((read = input.read(buffer)) > 0) {
            md.update(buffer, 0, read);
        }
        
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        
        return sb.toString();
    }
    
    /**
     * Remove caracteres Unicode inválidos para YAML 1.5.2
     */
    private static String sanitizeYamlContent(String content) {
        if (content == null) return "";
        
        // Remove BOM se presente
        if (content.startsWith("\uFEFF")) {
            content = content.substring(1);
        }
        
        // Substitui caracteres Unicode por equivalentes ASCII
        StringBuilder sb = new StringBuilder();
        for (char c : content.toCharArray()) {
            if (c < 128) { // Mantém apenas caracteres ASCII
                sb.append(c);
            } else if (c == '§' || c == '&') { // Mantém códigos de cores
                sb.append(c);
            } else {
                sb.append('?'); // Substitui outros caracteres Unicode
            }
        }
        
        return sb.toString();
    }

    public static void saveResourceIfDifferent(Plugin plugin, String resourcePath, File targetFile, boolean replace) {
        if (!targetFile.exists() || replace) {
            try {
                InputStream in = plugin.getResource(resourcePath);
                if (in == null) {
                    plugin.getLogger().warning("Recurso não encontrado: " + resourcePath);
                    return;
                }

                String content = readInputStream(in);
                content = sanitizeYamlContent(content); // Sanitiza antes de salvar
                
                // Verifica se o arquivo existe e é diferente
                if (targetFile.exists()) {
                    String existingContent = readFile(targetFile);
                    existingContent = sanitizeYamlContent(existingContent);
                    
                    if (content.equals(existingContent)) {
                        return; // Conteúdo é igual, não precisa salvar
                    }
                }

                // Salva o arquivo sanitizado
                Files.write(targetFile.toPath(), content.getBytes(StandardCharsets.UTF_8));
                
            } catch (Exception e) {
                plugin.getLogger().severe("Erro ao salvar recurso " + resourcePath + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Mescla dois arquivos YAML, preservando os valores personalizados do arquivo destino
     * e adicionando novas chaves do arquivo fonte
     * 
     * @param plugin Plugin que contém o recurso
     * @param resourcePath Caminho do recurso dentro do plugin
     * @param outputFile Arquivo de destino
     * @return true se a mesclagem foi bem-sucedida
     */
    private static boolean mergeYamlFiles(Plugin plugin, String resourcePath, File outputFile) {
        try {
            // Carregar arquivo fonte (do jar)
            InputStream resourceStream = plugin.getResource(resourcePath);
            if (resourceStream == null) {
                return false;
            }
            
            // Criar um arquivo temporário para o recurso
            File tempFile = File.createTempFile("resource_", ".yml");
            
            // Copiar o conteúdo do recurso para o arquivo temporário
            FileOutputStream outStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[8192];
            int read;
            
            while ((read = resourceStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, read);
            }
            
            resourceStream.close();
            outStream.close();
            
            // Carregar configurações usando os arquivos
            YamlConfiguration sourceConfig = YamlConfiguration.loadConfiguration(tempFile);
            YamlConfiguration targetConfig = YamlConfiguration.loadConfiguration(outputFile);
            
            // Mesclar chaves e seções
            mergeYamlConfigurations(sourceConfig, targetConfig);
            
            // Salvar o resultado
            targetConfig.save(outputFile);
            
            // Remover o arquivo temporário
            tempFile.delete();
            
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao mesclar arquivos YAML: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Mescla duas configurações YAML, preservando os valores existentes
     * e adicionando novas chaves da fonte para o destino
     * 
     * @param source Configuração fonte
     * @param target Configuração destino
     */
    private static void mergeYamlConfigurations(YamlConfiguration source, YamlConfiguration target) {
        for (String key : source.getKeys(false)) {
            if (source.isConfigurationSection(key)) {
                // Lidar com seções recursivamente
                if (!target.isConfigurationSection(key)) {
                    target.createSection(key);
                }
                mergeConfigurationSections(source.getConfigurationSection(key), target.getConfigurationSection(key));
            } else if (!target.contains(key)) {
                // Apenas adicionar chaves que não existem no destino
                target.set(key, source.get(key));
            }
        }
    }
    
    /**
     * Mescla duas seções de configuração recursivamente
     * 
     * @param source Seção fonte
     * @param target Seção destino
     */
    private static void mergeConfigurationSections(ConfigurationSection source, ConfigurationSection target) {
        for (String key : source.getKeys(false)) {
            if (source.isConfigurationSection(key)) {
                // Lidar com subseções recursivamente
                if (!target.isConfigurationSection(key)) {
                    target.createSection(key);
                }
                mergeConfigurationSections(source.getConfigurationSection(key), target.getConfigurationSection(key));
            } else if (!target.contains(key)) {
                // Apenas adicionar chaves que não existem no destino
                target.set(key, source.get(key));
            }
        }
    }

    /**
     * Lê o conteúdo de um InputStream como string
     * 
     * @param inputStream Stream de entrada para ler
     * @return Conteúdo como string
     * @throws IOException Se ocorrer um erro de leitura
     */
    private static String readInputStream(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
            return result.toString();
        }
    }
    
    /**
     * Lê o conteúdo de um arquivo como string
     * 
     * @param file Arquivo para ler
     * @return Conteúdo como string
     * @throws IOException Se ocorrer um erro de leitura
     */
    private static String readFile(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }
} 