# Living World — Visão Futura e Banco de Ideias

- **Estado:** ideação consolidada; não é escopo ativo, milestone, compromisso de entrega ou critério de aceite.
- **Autoridade de escopo:** `PRODUCT.md`, `TASKLIST.md`, `ROADMAP.md`, `DESIGN.md` e `PROJECT_STATE.md` continuam sendo as fontes canônicas para trabalho autorizado e estado operacional.
- **Objetivo deste documento:** preservar ideias de produto e direções arquiteturais discutidas para que possam ser revisitadas, refinadas, descartadas ou promovidas formalmente no futuro sem depender do histórico do chat.
- **Regra de promoção:** nenhuma ideia abaixo vira implementação, tarefa ou dependência obrigatória por existir neste arquivo. Promoção exige decisão explícita e atualização da fonte canônica apropriada.

## Norte do produto

Living World pode crescer como uma **expansão modular de Survival Vanilla+ server-side**: um plugin capaz de reunir muitas funcionalidades, desde que cada uma tenha ownership claro, seja opcional/configurável, componha com o restante do produto e respeite os limites de performance e compatibilidade do projeto.

A ambição não é evitar amplitude. É evitar amplitude incoerente. Um Living World com dezenas ou até centenas de pequenas capacidades pode ser desejável se continuar obedecendo a alguns princípios:

- o mundo deve parecer **vivo, reativo, histórico e evolutivo**, não apenas mais barulhento;
- mudanças sutis e acumulativas importam tanto quanto sistemas grandes;
- gameplay, QoL, apresentação e worldgen devem continuar conceitualmente separados;
- features não devem depender de scans globais ou trabalho proporcional ao tamanho total do mundo;
- mudanças de balanceamento devem ser assumidas como gameplay, nunca escondidas sob o rótulo de QoL ou otimização;
- integração com datapack/resourcepack/worldgen externo deve preservar o plugin como autoridade do comportamento que ele realmente possui;
- compatibilidade com clientes normais continua sendo uma prioridade do produto base.

Uma frase que resume a visão:

> **O mundo reage, lembra e envelhece.**

## Mapa conceitual

```text
                         LIVING WORLD

        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
   WORLD MEMORY       LIVING ECOLOGY     WORLD PHENOMENA
        │                  │                  │
   Discovery           Phenology          Rare events
   Chronicle           Hydric memory      Calendar events
   Landmarks           Recovery           Atmosphere
   World identity      Fauna rhythms      Seasonal moments
   Human presence      Disturbances       Memorable nights
        │                  │                  │
        └──────────────────┼──────────────────┘
                           ▼
                   LIVING SURVIVAL WORLD
                           │
          ┌────────────────┼────────────────┐
          ▼                ▼                ▼
        QoL          Exploration       Runic/Fantasy
```

Os blocos abaixo são famílias conceituais, não módulos obrigatórios nem sequência de milestones.

---

## 1. World Memory — o mundo se lembra

### 1.1 Crônica do Mundo

A ideia central é criar um `WorldChronicle` separado do sistema de Discovery.

`Discovery` responde a perguntas como:

- este jogador já descobriu este bioma?
- este mundo já descobriu este landmark?
- esta Waystone já foi descoberta por este jogador?

`WorldChronicle` responderia a outra pergunta:

> **O que aconteceu neste mundo ao longo do tempo?**

Exemplos de entradas possíveis:

- `Primeiro inverno — Ano 1`;
- `A Waystone Vale do Norte foi descoberta`;
- `Primeiro jogador chegou ao End`;
- `A Fortaleza Cinzenta foi descoberta no Outono do Ano 2`;
- `Grande tempestade do Ano 3`;
- `Primeira neve do Inverno do Ano 4`;
- `Grande Aurora — Inverno, Ano 4`;
- `Incêndio da Floresta Norte`;
- `A Floresta Norte começou a se recuperar`.

A Crônica não deveria transformar todo evento em log histórico. Uma classificação conceitual de importância pode evitar ruído:

```text
EPHEMERAL  -> chuva comum, pequenos acontecimentos; não registrar
NOTICEABLE -> primeira neve, transição sazonal especial
MEMORABLE  -> aurora rara, tempestade excepcional, landmark relevante
HISTORIC   -> primeira ida ao End, grandes marcos de exploração/civilização
```

A decisão sobre thresholds, persistência, retenção, texto e quais produtores podem publicar eventos é futura. O princípio importante é manter **Discovery e Chronicle separados** para não contaminar a identidade simples e idempotente de Discovery com timestamps, narrativa ou histórico.

### 1.2 World Identity

Depois de meses de jogo, o mundo poderia possuir uma identidade descritiva derivada de fatos reais, sem necessariamente conceder buffs ou alterar gameplay.

Exemplo conceitual:

```text
Idade do mundo: 6 anos
Estações severas: 3 invernos intensos
Secas relevantes: 2
Biomas descobertos: 47
Landmarks descobertos: 18
Waystones ativas: 12
Eventos memoráveis: 23
```

Isso permitiria que um save longo parecesse único e tivesse uma biografia própria.

### 1.3 Aniversários e memória temporal

A Crônica poderia eventualmente produzir lembranças de aniversário sem recompensa obrigatória:

- `Hoje faz 1 ano que a Fortaleza Cinzenta foi descoberta`;
- `Há 3 anos a primeira Waystone deste mundo foi criada`;
- `Este é o quinto inverno desde a fundação do mundo`.

O valor é emocional e histórico, não necessariamente mecânico.

### 1.4 Discovery Journal / Atlas

Com a evolução de Discovery para biomas, landmarks, Waystones e outras categorias, uma superfície de consulta futura pode resumir o que foi encontrado.

Exemplo:

```text
Livro do Explorador

Biomas descobertos: 23
Landmarks descobertos: 7
Waystones conhecidas: 5
```

Essa superfície pode existir como comando, livro, menu, UI opcional ou integração futura. Ela não deve transformar Discovery automaticamente em quest log ou sistema de recompensas.

---

## 2. Living Ecology — estações que realmente significam algo

### 2.1 Fenologia

A evolução natural da ecologia sazonal é modelar **quando** fenômenos biológicos acontecem, não apenas calcular temperatura ou alterar visual.

Perguntas que uma camada de fenologia poderia responder:

- quando determinada planta cresce melhor?
- quando flores aparecem?
- quando berries/frutos são favorecidos?
- quando cogumelos se tornam mais comuns?
- quando saplings têm melhores condições?
- quando vegetação entra em dormência?
- quando folhas caem ou mudanças sazonais ficam perceptíveis?

Exemplo de leitura de estação:

```text
Primavera
- mais flores e cobertura vegetal
- crescimento de saplings favorecido
- reprodução de algumas espécies favorecida

Verão
- crops e frutos favorecidos
- maior risco de seca/incêndio em regiões quentes

Outono
- folhas/partículas sazonais
- fungos favorecidos em ambientes adequados
- desaceleração gradual da vegetação

Inverno
- crescimento reduzido
- neve/gelo persistem em condições adequadas
- algumas plantas entram em dormência
```

A implementação futura deve preferir a filosofia já usada pelo projeto: **reagir a eventos/tentativas vanilla e aplicar políticas**, em vez de criar um simulador global que percorre o mundo.

### 2.2 Memória hídrica/climática local

Clima instantâneo e umidade de biome não precisam ser a única fonte de contexto. Uma região poderia manter memória transitória e esparsa de eventos recentes:

```text
chuva forte
   ↓
recentemente molhado
   ↓
úmido
   ↓
normal
   ↓
seco
```

Essa memória poderia influenciar somente políticas que já entendem umidade, por exemplo:

- propagação de fogo;
- retenção de moisture em farmland;
- crescimento de determinados tipos de vegetação;
- fungos e efeitos ambientais;
- risco de seca.

O objetivo não é simular água. É introduzir causalidade:

> **choveu aqui recentemente, portanto o ambiente continua refletindo essa chuva por algum tempo.**

O estado deve ser esparso, bounded e baseado em regiões realmente relevantes/observadas.

### 2.3 Distúrbio e recuperação

Living World já possui conceitos que apontam nessa direção: fogo reage ao clima e Desire Lines podem se recuperar. Isso pode evoluir para uma linguagem comum de `EnvironmentalDisturbance` sem exigir um motor de simulação global.

Tipos conceituais possíveis:

```text
FIRE
DROUGHT
FROST
HEAVY_TRAFFIC
STORM
EXTREME_RAIN
```

Um incêndio real poderia deixar uma memória temporária apenas na área efetivamente afetada. A recuperação poderia ocorrer em fases:

```text
incêndio
   ↓
solo / vegetação danificada
   ↓
vegetação pioneira
   ↓
cobertura vegetal em recuperação
   ↓
normalização
```

O princípio é importante: **acompanhar o que realmente foi alterado**, em vez de descobrir dano ou recuperação por scans do mapa.

### 2.4 Ritmos de fauna

A fauna pode reagir a estação/clima sem que Living World controle centenas de entidades com IA própria.

Primeiras direções possíveis:

- influenciar oportunidades vanilla de reprodução de espécies específicas;
- modular alguns spawns naturais por estação/clima quando houver API segura e impacto de balanceamento aceito;
- representar períodos de maior/menor atividade;
- usar eventos e tentativas já produzidos pelo jogo sempre que possível.

Exemplos conceituais:

- primavera favorece determinadas oportunidades de reprodução;
- ambientes úmidos favorecem espécies adequadas;
- inverno reduz algumas atividades;
- uma seca prolongada altera determinadas oportunidades locais.

Qualquer mudança de spawn/reprodução é gameplay/balanceamento e deve ser configurável e documentada como tal.

### 2.5 Selvagem versus habitado

Uma ideia experimental é derivar presença humana de fatos que Living World já conhece ou pode conhecer de forma bounded:

- tráfego de Desire Lines;
- Waystones;
- atividade relevante de jogadores;
- landmarks descobertos/ocupados;
- estruturas ou sinais explicitamente registrados por features futuras.

Uma leitura interna poderia distinguir aproximadamente:

```text
WILDERNESS -> FRONTIER -> SETTLED
```

Inicialmente isso deveria influenciar **atmosfera e seleção de eventos**, não loot, dificuldade ou economia.

Exemplos:

- regiões selvagens podem receber mais fenômenos naturais discretos;
- regiões habitadas mostram caminhos estabelecidos e sinais de ocupação;
- certos efeitos atmosféricos podem preferir áreas pouco ocupadas.

Não deve existir scan de construções para calcular "civilização". O sistema deve derivar o estado apenas de sinais já rastreados de forma explícita.

---

## 3. World Phenomena — magia pela raridade e pela atmosfera

### 3.1 Sistema de fenômenos naturais raros

Uma abstração futura como `WorldPhenomenon` pode representar acontecimentos condicionais e raros.

Possíveis critérios:

```text
dimension
season
time
climate
weather
rarity
cooldown
local context
```

Exemplos de fenômenos:

- aurora em noites frias, claras e raras;
- noites de vagalumes em clima quente/úmido;
- estrelas cadentes ou chuva de meteoros;
- neblina atmosférica em condições adequadas;
- folhas carregadas pelo vento;
- primeira neve do inverno;
- tempestades excepcionais;
- Lua de Colheita;
- fenômenos mágicos extremamente raros.

Araridade faz parte da experiência. O objetivo não é um servidor que anuncia evento a cada poucos minutos, mas criar momentos que o grupo lembra meses depois.

### 3.2 Eventos de calendário

Há duas famílias conceituais:

**Previsíveis:**

- solstício;
- equinócio;
- mudança de estação;
- virada de ano/calendário.

**Emergentes:**

- primeira geada;
- primeira neve;
- noite excepcionalmente fria;
- grande tempestade;
- aurora rara;
- seca prolongada;
- evento astronômico.

Esses eventos podem alimentar apresentação, ecologia e Chronicle sem que um sistema precise possuir internamente os outros.

Fluxo conceitual:

```text
Phenomenon / Calendar Event
            │
            ▼
        WorldEvent
        ┌────┴────┐
        ▼         ▼
   presentation  Chronicle
```

### 3.3 Living, not noisy

Fenômenos devem obedecer um orçamento de atenção. Eventos comuns podem ser quase invisíveis; eventos raros merecem apresentação maior. Um evento excepcional deixa de ser excepcional se ocorrer toda sessão.

---

## 4. QoL — muitas pequenas melhorias podem formar uma ótima experiência

A amplitude de QoL é desejável quando cada capacidade preserva intenção do jogador e evita automação silenciosa.

### 4.1 Ideias já alinhadas à identidade do produto

- Double Doors;
- Desire Lines e recuperação de caminhos;
- Waystones;
- Hotbar Auto-Refill conservador;
- Deposit Matching;
- Container Sort.

O estado real dessas capacidades pertence às fontes canônicas e às branches/tarefas correspondentes; este documento não tenta duplicar status operacional.

### 4.2 Restock Matching

Complemento conceitual ao trio de storage QoL:

```text
Deposit Matching -> leva para o storage categorias já existentes nele
Container Sort   -> organiza explicitamente o storage aberto
Restock Matching -> completa stacks elegíveis do jogador a partir do storage
```

Se promovido, deve manter limites conservadores semelhantes: gesto explícito, target conhecido, sem rede de storage, sem scan de containers próximos e sem mexer em slots deliberadamente protegidos sem uma regra clara.

### 4.3 Graves

Opção futura entre morte vanilla e `keepInventory`: criar um túmulo que preserva itens para recuperação pelo jogador.

A intenção é reduzir frustração sem remover totalmente a consequência da morte. Deve ser uma escolha explícita de gameplay/QoL, não comportamento obrigatório.

### 4.4 Harvest / replant

Colheita e replantio explícitos podem reduzir microgerenciamento. A feature deve evitar automação de fazendas e respeitar o balanceamento definido pelo servidor.

### 4.5 Leaf decay acelerado

Pode ser QoL de baixo impacto quando implementado de forma bounded e sem scans, reduzindo a espera por folhas após corte de árvores.

### 4.6 Árvores físicas / tree felling

"Árvore caindo" e corte em massa são mais próximos de imersão/gameplay do que QoL neutro. Se existirem, devem ser classificados corretamente e respeitar proteção, construções com logs e limites rígidos.

---

## 5. Runic / Fantasy Gameplay

Living World pode desenvolver uma camada de fantasia server-side sem precisar virar um modpack completo.

A pesquisa de Runic Tools já apontou uma linguagem conceitual promissora:

```text
Runa       -> define a capacidade
Ressonância -> energia finita armazenada na ferramenta
Recarga     -> restaura essa reserva
```

Primeiras famílias estudadas/concebidas:

- Vein Mining com custo real, durabilidade e limites;
- Tree Felling com reconhecimento conservador de árvore natural.

Expansões futuras possíveis, sem compromisso:

- Rune of Harvest;
- Rune of Frost;
- Rune of Ember;
- Rune of Storms;
- Rune of Echoes.

Princípio arquitetural: uma runa pode **consultar** contexto ambiental quando isso fizer sentido, mas Ecology/Climate não deve virar dependência acidental de todo sistema mágico. Features precisam continuar degradando de forma coerente quando módulos independentes estão desativados.

Também não há decisão de adicionar mana global ao jogador. Uma barra universal de mana só faria sentido quando existir um ecossistema de magia amplo o suficiente para justificar ownership, regeneração, HUD, progressão e balanceamento próprios.

---

## 6. Exploration & Discovery

Discovery pode crescer como infraestrutura aberta para diferentes produtores sem se tornar um sistema monolítico.

Direções discutidas:

- descoberta pessoal de biomas;
- landmarks com memória de mundo;
- vilas/ruínas/estruturas relevantes;
- Waystones naturais ou encontradas;
- Discovery Journal / Atlas;
- Travel Documents;
- apresentação diferente por tipo de descoberta;
- world-scoped discoveries com política social/broadcast futura.

Custom worldgen deve entrar naturalmente por chaves/registries/tags quando as APIs permitirem, sem tabelas rígidas de nomes de Terralith ou outros projetos.

---

## 7. Worldgen externo e composição de mundos

Living World **não deve recriar Terralith, Tectonic ou uma plataforma de worldgen pesada**. Entretanto, o produto pode ser uma excelente camada de comportamento sobre mundos gerados por outras ferramentas.

Superfícies de compatibilidade já discutidas incluem combinações ou projetos como:

- Terralith / Tectonic / Terratonic no Overworld;
- Incendium ou alternativas para o Nether;
- Nullscape e outras soluções para o End;
- outros datapacks/plugins de estruturas, vilas e exploração.

Esses nomes são exemplos de compatibilidade/pesquisa, não dependências permanentes.

### 7.1 Presets de mundo pertencem principalmente à camada de composição

Conceitos de experiência discutidos:

- **Vanilla Vivo:** Minecraft reconhecível, pequenas melhorias, seasons leves, caminhos, vilas/estruturas discretas e QoL;
- **Fantasia Viva:** mundo transformado, Nether/End enriquecidos por componentes externos, estruturas, Waystones, seasons, clima, ambiente vivo e QoL;
- **High Fantasy:** mais progressão, bosses, skills, loot e magia;
- **Dark Fantasy:** atmosfera hostil, ruínas, clima severo, eventos sombrios e maior risco;
- **Arcano Industrial:** fantasia + tecnologia/automação por componentes apropriados.

Esses presets não precisam morar como hardcode do plugin. Em especial, composição de múltiplos datapacks/plugins/worldgen pode pertencer ao Launcher/orquestrador futuro. Living World deve fornecer módulos e contratos que possam participar dessas experiências.

---

## 8. Integração futura com o Launcher

Sem adicionar sockets, HTTP ou acoplamento agora, é desejável preservar read models e contratos que permitam futura integração do Living World com a aba de Servidores do Launcher.

Informações interessantes para apresentação futura:

```text
Ano / mês / dia
estação atual
clima e temperatura local/representativa
evento ambiental atual
fenômeno raro ativo
últimas entradas da Chronicle
número de discoveries/landmarks
World Identity resumida
estado de módulos
métricas ambientais agregadas
```

Princípio:

> **o plugin continua sendo a fonte de verdade do domínio; o Launcher apresenta, administra e orquestra.**

Uma UI futura poderia mostrar, por exemplo:

```text
Mundo dos Amigos
Ano 5 • Outono • Dia 19
Chuva • 17 °C

Últimos acontecimentos
- Grande Aurora
- Fortaleza Sombria descoberta
- Inverno do Ano 4 terminou
```

Essa integração é visão de produto, não requisito atual de protocolo ou rede.

---

## 9. Performance e arquitetura para um Living World amplo

Ter muitas features só é sustentável se o custo continuar previsível.

### 9.1 Regras estruturais

- nenhum scan global periódico de chunks, blocos ou entidades;
- preferir eventos do jogo, deltas, mudanças reais e estado esparso;
- simular apenas regiões/jogadores relevantes;
- quando uma área sofre distúrbio, acompanhar somente essa área;
- trabalho ambiental adiável deve ser bounded e possuir cadence/owner/cleanup;
- persistência só quando o benefício justificar migração, recovery e custo operacional;
- não alegar ganho de performance sem medição.

### 9.2 Orçamento de simulação

Uma direção futura é classificar trabalho por criticidade:

```text
GAMEPLAY_CRITICAL  -> combate, ticks essenciais; não degradar
GAMEPLAY_TEMPORAL  -> crops/seasonal behavior; cuidado
AMBIENTAL          -> vegetação, erosão, fenômenos; pode reduzir cadence
BACKGROUND         -> análise, preparação, tarefas auxiliares; pode pausar
```

Se houver evidência suficiente no futuro, um budget ambiental pode reduzir trabalho não essencial quando MSPT/CPU estiverem pressionados sem alterar silenciosamente o gameplay essencial.

Exemplo conceitual:

```text
MSPT saudável
-> ecologia/ambiente em cadence normal

MSPT pressionado
-> reduzir trabalho ambiental adiável
-> pausar background
-> preservar tick/gameplay crítico
```

Isso é uma direção de arquitetura; thresholds e adaptação real exigem profiling e benchmark antes de qualquer implementação.

---

## 10. Relações entre as ideias

Uma possível dependência conceitual, sem virar roadmap:

```text
Calendar + Climate + Discovery
            │
     ┌──────┼───────────┐
     ▼      ▼           ▼
 Chronicle Phenology  Phenomena
     │      │           │
     │      ├── Hydric Memory
     │      ├── Fauna Rhythms
     │      └── Disturbance/Recovery
     │                  │
     └──────────┬───────┘
                ▼
          World Identity
                │
        Launcher presentation
```

`Human Presence / Wilderness` pode consumir sinais de Desire Lines, Waystones, atividade e outras features, mas deve continuar como leitura derivada e bounded.

Runic/Fantasy e Inventory QoL são trilhas laterais: podem compor com o mundo, mas não precisam depender do eixo ambiental para existir.

---

## 11. Ideias que deliberadamente não estão decididas

Este documento não decide:

- milestones ou IDs de tarefas;
- ordem de implementação;
- datas;
- formatos de banco/persistência;
- APIs públicas finais;
- thresholds de clima, rarity, simulation budget ou balanceamento;
- recipes/custos de runas;
- lista definitiva de fenômenos;
- lista definitiva de plugins/datapacks/worldgen compatíveis;
- bundling ou redistribuição de componentes de terceiros;
- resource pack obrigatório versus opcional por feature;
- suporte a clientes/modloaders além do contrato atual do produto;
- que todas as ideias serão implementadas.

Quando uma ideia for promovida, a decisão deve ser registrada na fonte adequada e decomposta em um slice verificável.

---

## 12. Resumo da identidade futura

Living World pode ser amplo sem perder foco se cada feature reforçar uma das sensações abaixo:

1. **O mundo reage.** Clima, estação, tráfego e ações deixam consequências.
2. **O mundo muda.** Ecologia, fenômenos e recuperação fazem o ambiente evoluir.
3. **O mundo lembra.** Discovery, Chronicle e World Identity preservam história.
4. **O mundo recompensa exploração.** Biomas, landmarks, Waystones e documentos tornam lugares significativos.
5. **O mundo é confortável de jogar.** QoL remove atrito sem automatizar tudo.
6. **O mundo pode ser mágico.** Runas e fenômenos acrescentam fantasia sem exigir um modpack base.
7. **O mundo continua Minecraft.** Vanilla+ significa enriquecer a sandbox, não substituir sua identidade.

Essa é a função deste arquivo: manter viva a visão ampla sem confundi-la com o trabalho ativo.