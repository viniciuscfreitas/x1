# PrimeLeagueX1

Um plugin completo de duelos PvP para servidores Minecraft (Bukkit/Spigot).

## Características

- Sistema de duelos PvP individuais (X1) e em equipes (2v2, 3v3, etc.)
- Interface gráfica intuitiva para gerenciar desafios
- Sistema de arenas configuráveis
- Sistema de estatísticas com rank de jogadores
- Sistema de apostas em duelos
- Suporte a kits de duelo configuráveis
- Integração com WorldGuard para proteção de arenas
- Integração com Vault para economia
- Sistema de equipes com prefixos e cores configuráveis
- Efeitos visuais e sonoros durante duelos
- Sistema de logs de duelos com registro detalhado

## Novos Recursos

### Efeitos Visuais e Sonoros
- Títulos e mensagens durante a contagem regressiva
- Efeitos sonoros para início, vitória, derrota e empate em duelos
- Efeitos visuais (fogos de artifício) para celebrar vitórias
- Sistema de notificações aprimorado com sons

### Configuração Melhorada
- Arquivo de configuração reorganizado e documentado
- Mensagens customizáveis com prefixo visual atraente
- Sons configuráveis para vários eventos

## Comandos

- `/x1` - Abre o menu principal de duelos
- `/x1 desafiar <jogador>` - Desafia um jogador para duelo
- `/x1 aceitar <jogador>` - Aceita um desafio pendente
- `/x1 rejeitar <jogador>` - Rejeita um desafio pendente
- `/x1 stats [jogador]` - Mostra estatísticas do jogador
- `/x1 top` - Mostra o ranking de melhores jogadores
- `/x1 arena <set|tp> <nome>` - Gerencia arenas (requer permissão)
- `/x1 admin` - Comandos administrativos (requer permissão)

## Permissões

- `primeleaguex1.use` - Permite usar o plugin
- `primeleaguex1.admin` - Permite usar comandos administrativos
- `primeleaguex1.arena` - Permite gerenciar arenas
- `primeleaguex1.team` - Permite criar e gerenciar equipes

## Instalação

1. Coloque o arquivo .jar na pasta plugins do seu servidor
2. Reinicie o servidor
3. Configure as arenas usando `/x1 arena set <nome>`
4. Personalize as configurações no arquivo config.yml

## Dependências

- Vault (opcional, para economia)
- WorldGuard (opcional, para proteção de arenas)
- PrimeLeagueElo (opcional, para sistema de Elo)

## Configuração

O plugin inclui arquivos de configuração detalhados:

- `config.yml` - Configurações gerais, arenas, kits, sons
- `messages.yml` - Mensagens customizáveis

## Contato e Suporte

Para suporte ou sugestões, entre em contato através do Discord PrimeLeague. 