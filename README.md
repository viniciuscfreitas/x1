# PrimeLeagueX1

Plugin de duelos 1v1 para servidores Minecraft.

## Funcionalidades

- **Duelos com diferentes tipos:**
  - Duelos na arena
  - Duelos locais (no mesmo lugar)
  - Duelos com kit pré-definido ou com itens próprios

- **Sistema de Apostas:**
  - Suporte para apostas em duelos utilizando economia do servidor

- **Sistema de Arenas:**
  - Configure posições para duelos em arenas
  - Teleporte automático para arenas
  - Retorno automático ao local original após duelo

- **Interface Gráfica (GUI):**
  - Menus interativos para enviar desafios
  - Visualização de estatísticas

- **Sistema de Times:**
  - Crie e gerencie times
  - Desafie outros times

## Comandos

- `/x1 desafiar <jogador> [tipo]` - Desafia um jogador para duelo
- `/x1 aceitar <jogador>` - Aceita um desafio
- `/x1 rejeitar <jogador>` - Rejeita um desafio
- `/x1 arena set1` - Define a posição 1 da arena
- `/x1 arena set2` - Define a posição 2 da arena
- `/x1 arena setespectador` - Define o local de espectador
- `/x1 menu` - Abre o menu principal

## Permissões

- `primeleaguex1.admin` - Acesso a comandos administrativos
- `primeleaguex1.duel` - Permissão para duelos
- `primeleaguex1.arena` - Permissão para configurar arena
- `primeleaguex1.spectate` - Permissão para assistir duelos

## Melhorias Recentes

- Correção de problemas de visibilidade entre jogadores
- Melhorias no sistema de teleporte para a arena
- Retorno automático à posição original após o duelo
- Verificações adicionais para garantir que os jogadores se vejam durante o duelo
- Sistema de times melhorado

## Configuração

As configurações do plugin podem ser encontradas nos arquivos:
- `config.yml` - Configurações gerais
- `messages.yml` - Mensagens customizáveis
- `kit.yml` - Configuração do kit padrão 