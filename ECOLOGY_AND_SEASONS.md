# Living World — Ecologia, Seasons e Arquitetura Ambiental

## Estado e autoridade

- **Estado:** direção arquitetural aceita pelo usuário em 2026-09-27; implementação além de M12.4 ainda pendente.
- **Escopo:** evolução da frente de ecologia, clima, temperatura, seasons e apresentação ambiental.
- **Autoridades superiores:** `PRODUCT.md` para intenção de produto, `DESIGN.md` para arquitetura geral, `TASKLIST.md` para execução e `ROADMAP.md` para sequência.
- **Função deste documento:** preservar decisões, objetivos, limites e arquitetura especializada desta frente sem criar uma segunda fonte concorrente.
- **Branch:** o usuário decidiu manter uma frente/branch dedicada à ecologia e usar a linha principal para outras áreas. No momento desta documentação o worktree observado ainda está em `master`; nenhuma criação/troca de branch é realizada por este documento.

## 1. Objetivo da frente ecológica

Living World deve evoluir de um conjunto de efeitos sazonais para um **sistema ambiental coerente**, em que calendário, estação, clima, temperatura, ecologia, apresentação e mudanças físicas compartilham a mesma verdade.

O objetivo de produto é competir em qualidade percebida, profundidade e eficiência com referências como:

- AdvancedSeasons;
- RealisticSeasons;
- Aeternum Seasons;
- Serene Seasons;
- ThermoSurvival.

Esses projetos são **benchmarks e fontes de inspiração**, não especificações a copiar. Living World deve provar suas próprias vantagens por comportamento observado e medição. Não declarar que é mais rápido ou melhor que concorrentes sem benchmark comparável.

Referências estudadas:

- https://github.com/AdvancedPlugins/Seasons — repositório público sob Apache-2.0;
- https://github.com/parlamentum/ThermoSurvival — referência de produto/temperatura; o README observado informa MIT, mas nenhuma licença raiz foi confirmada nesta sessão;
- https://github.com/KinkinxD12/Aeternum-Seasons — referência de produto; o README observado declara que o código-fonte ainda não foi aberto;
- https://www.curseforge.com/minecraft/mc-mods/serene-seasons — referência de experiência sazonal em mod client+server;
- RealisticSeasons — referência de experiência, projeção visual e temperatura.

Nenhum código de terceiros deve ser copiado apenas por estar publicamente visível. Reuso exige licença compatível, atribuição quando aplicável e necessidade real.

## 2. Princípio central

O plugin é a **única autoridade de comportamento**.

```text
Calendar / logical time
        |
        v
Season
        |
        v
ClimateSnapshot
        |
        v
Environmental State
        |
        +--> Ecology policies
        +--> Ambient temperature
        +--> Player thermal state
        +--> Visual projection
        +--> Physical ecology
```

Regras sazonais não devem ser duplicadas em listeners, datapack, resourcepack ou features independentes.

## 3. Plugin + datapack + resourcepack

Living World pode explorar três frentes, mas com ownership explícito.

### 3.1 Plugin Paper — cérebro e orquestrador

O plugin possui:

- calendário e estado lógico de seasons;
- clima e `ClimateSnapshot`;
- perfis ambientais por dimensão/bioma;
- temperatura ambiente;
- estado térmico do jogador;
- políticas ecológicas;
- decisões de neve, gelo, degelo, flora e riscos térmicos;
- projeção visual client-side quando tecnicamente segura;
- mudanças físicas do mundo quando gameplay exigir estado real;
- lifecycle, configuração, cache, budgets, métricas e diagnóstico;
- descoberta/verificação do datapack;
- entrega/verificação do resourcepack.

Datapack e resourcepack nunca calculam uma estação concorrente.

### 3.2 Datapack — dados Minecraft-native declarativos

O datapack complementar pode conter:

- tags próprias;
- predicates;
- metadados/classificações declarativas;
- biomas auxiliares/visuais se um spike técnico comprovar a estratégia;
- worldgen próprio somente se existir requisito futuro explícito;
- integração declarativa com registries vanilla.

O datapack **não** deve virar o motor de simulação. Evitar `tick.mcfunction` permanente executando lógica ambiental global, scans, fills ou loops que concorram com o plugin.

O plugin deve verificar o datapack no bootstrap/startup e sua versão/schema. Não usar reload de datapack como mecanismo normal de troca de estação.

### 3.3 Resourcepack — apresentação

O resourcepack contém apresentação, não verdade de gameplay:

- texturas sazonais;
- assets de atmosfera;
- sons de vento, frio, calor e ambiente;
- partículas/assets customizados quando suportados;
- UI e ícones;
- feedback térmico refinado.

Deve existir **um único pack** contendo assets das quatro estações; não trocar o ZIP inteiro a cada mudança de season.

O plugin gerencia:

```text
PlayerJoin
  -> request pack + hash
  -> observe status
  -> LOADED / DECLINED / FAILED
```

O gameplay principal deve continuar coerente sem resourcepack, usando fallback vanilla quando possível. Servidores podem futuramente optar por exigir o pack, mas isso é configuração explícita.

### 3.4 Contratos e versionamento

Plugin, datapack e resourcepack devem usar versão de contrato/schema, não depender apenas de igualdade textual de versões.

Exemplo conceitual:

```text
plugin 1.4.2
datapack 1.4.0 -> schema 3
resourcepack 1.3.8 -> schema 5

plugin espera datapack schema 3 e resource schema 5
=> compatível
```

Manifests devem permitir diagnóstico de versão, schema, hash e compatibilidade.

**Checkpoint de implementação LW-124 / primeiro slice:** o core agora possui um contrato dependency-free para companions em `living-world-pack.properties`. O manifest contém `kind=datapack|resourcepack`, `version`, `schema` e `sha256` opcional. `CompanionPackManifestCodec` lê/escreve esse formato sem biblioteca externa.

`CompanionPackContractPolicy` verifica **tipo + schema**; versões humanas diferentes continuam compatíveis quando o schema esperado coincide. Hash declarado só é comparado quando bytes/hash observado estão disponíveis. Ausência possui semântica explícita de `OPTIONAL_MISSING` ou `REQUIRED_MISSING`, e os defaults atuais mantêm datapack/resourcepack opcionais até os próximos slices de bootstrap/delivery.

`LivingWorldCompanionContracts` centraliza os schemas esperados atuais: datapack schema 1 e resourcepack schema 1. Isso é contrato do plugin, não evidência de que packs físicos já existam ou estejam instalados.

**Segundo slice:** `DatapackRuntimePolicy` separa `OPTIONAL_MISSING`, `REQUIRED_MISSING`, `INCOMPATIBLE`, `COMPATIBLE_DISABLED` e `COMPATIBLE_ENABLED`. Assim manifest compatível não é confundido com datapack realmente ativo no Paper.

Resourcepack usa state machine própria por UUID de requisição. `ResourcePackClientSession` começa em `REQUESTED` e pode refletir `ACCEPTED`, `DOWNLOADED`, `LOADED`, `DECLINED`, `FAILED_DOWNLOAD`, `INVALID_URL`, `FAILED_RELOAD` e `DISCARDED`. Eventos com request ID diferente são ignorados e um estado terminal não é reaberto por evento atrasado.

Para pack opcional, um estado terminal sem `LOADED` ativa fallback vanilla e mantém o contrato de gameplay coerente. Para pack configurado como obrigatório, decline/failure não é considerado contrato satisfeito; o adapter Paper/servidor decidirá a ação operacional, sem inventar essa decisão na policy de domínio.

**Terceiro slice:** `PaperResourcePackStatusMapper` espelha todos os estados públicos atuais de `PlayerResourcePackStatusEvent` para o domínio, e `PaperResourcePackStatusModule` só aplica eventos a uma sessão previamente registrada por `ResourcePackSessionStore`. Isso impede que um resourcepack do `server.properties` ou de outro plugin seja confundido com a requisição do Living World.

O store é efêmero por UUID do jogador, substitui sessão anterior quando uma nova requisição Living World é registrada e limpa em quit/disable. O módulo já participa do lifecycle, mas permanece dormente enquanto delivery não registrar requests.

`PaperDatapackRuntimeResolver` recebe uma compatibilidade já validada e apenas consulta `DatapackManager#getPack(name)` + `isEnabled()`. Ele **não chama `refreshPacks()`**, não habilita/desabilita pack e não executa reload. Bootstrap ativo continua para o próximo slice.

**Quarto slice:** manifests físicos são procurados somente em caminhos fixos do data folder do plugin:

    plugins/LivingWorld/companions/datapack/living-world-pack.properties
    plugins/LivingWorld/companions/resourcepack/living-world-pack.properties

`companions.datapack.enabled=false` por default. Quando opt-in, `PaperDatapackContractModule` valida o manifest e observa o datapack previamente instalado pelo operador através do nome Paper configurado. Ele continua sem copiar arquivo, sem `refreshPacks()`, sem `setEnabled()` e sem reload. Se `required=true`, manifest ausente/incompatível ou pack ausente/disabled falham o bootstrap antes dos módulos de gameplay.

`companions.resource-pack.enabled=false` por default. Quando opt-in e o manifest schema 1 é compatível, `PaperResourcePackDeliveryModule` envia **um único pack** usando a API Adventure moderna `ResourcePackRequest`, registra UUID próprio por jogador e deixa `PaperResourcePackStatusModule` acompanhar o lifecycle. Jogadores online no enable e joins posteriores recebem a mesma configuração.

O hash do protocolo Minecraft é explicitamente separado do manifest: delivery exige `sha1` de 40 hex chars para caching do cliente; o `sha256` opcional do manifest continua sendo integridade do artifact/contrato e nunca é reutilizado silenciosamente como SHA-1.

Pack opcional sem manifest compatível não é enviado e cai para apresentação vanilla. Pack obrigatório inválido falha o módulo de forma explícita. Nenhum ZIP sazonal é trocado por season; o contrato é para um único resourcepack contendo todos os assets sazonais quando esses assets forem produzidos.

Com isso, LW-124 fica **implementado, mas ainda não validado com artifacts reais**. Não existe datapack/resourcepack físico nesta fatia; portanto não há alegação de que conteúdo, hosting ou download reais já foram testados.

## 4. Compatibilidade de worldgen como requisito de arquitetura

Ambiente-alvo oficial para desenvolvimento e smoke:

```text
Overworld: Terralith + Tectonic
Nether:    Incendium
End:       Nullscape
```

Compatibilidade não deve depender de hardcode obrigatório dos nomes desses projetos.

### 4.1 Regra de fallback

Um biome namespaced desconhecido deve continuar funcionando:

```text
Namespaced biome
  -> propriedades ambientais observáveis
  -> dimensão
  -> temperatura/humidade coordenada
  -> altitude/exposição
  -> perfil climático conservador
```

Perfis conhecidos de Terralith/Incendium/Nullscape podem refinar o resultado, mas não ser pré-condição para funcionamento.

**Checkpoint LW-125 / slice 1:** `EnvironmentalDimensionPolicy` deriva comportamento apenas de `World.Environment`: NORMAL mantém season/day/weather/ecology/frozen surfaces terrestres; NETHER/THE_END/CUSTOM zeram os modificadores sazonais/diurnos/weather e desabilitam ecology/frozen-surface flags, preservando a temperatura coordenada observada pelo Paper.

**Slice 2:** `PaperWorldgenAmbientTemperatureResolver` aplica esse profile ao bloco no mundo atual do jogador. `PaperThermalRuntimeModule` e `PaperThermalFeedbackModule` deixaram de iterar um único `World` configurado e agora percorrem `Server#getOnlinePlayers()`, permitindo que o mesmo runtime corporal atravesse Overworld, Nether, End e mundos CUSTOM sem trocar de motor.

`PaperAmbientTemperatureProvider` desacopla o consumidor térmico da implementação de clima local. O resolver legado do Overworld continua implementando essa interface, enquanto o novo resolver multiworld usa `World#getTemperature(x,y,z)`, ambiente Paper, skylight, hora/weather quando permitidos pelo profile e o mesmo calendário global. Nenhum namespace de biome/mod é consultado neste slice.

**Slice 3:** regressões dedicadas fixam o fallback property-based como contrato. `PaperClimateSamplerTest` classifica um cenário de worldgen customizado somente por temperatura/umidade coordenadas, e `PaperWorldgenAmbientTemperatureResolverTest` resolve um Overworld customizado sem consultar biome ou namespaced key. Os proxies falham em qualquer método não explicitamente permitido, então uma futura introdução silenciosa de tabela por biome quebra o teste.

**Slice 4:** a matriz automatizada cobre explicitamente as quatro categorias Paper. NORMAL preserva season/day/weather; NETHER com temperatura coordenada `2.0` resulta em 39 °C sem ganho sazonal/diurno/weather terrestre; THE_END com `0.5` resulta em 9 °C sem sazonalidade terrestre; CUSTOM mantém o fallback conservador. Assim calor do Nether e frio/neutralidade de outros mundos continuam derivados das propriedades do worldgen, não de offsets por nome.

Com esse slice, LW-125 fica **implementado, mas ainda não validado com a stack real**. O aceite final exige smoke em Terralith + Tectonic, Incendium e Nullscape para confirmar que esses worldgens publicam propriedades Paper coerentes no runtime real.

### 4.2 Tectonic

Tectonic deve ser tratado principalmente como geometria real do mundo, não como uma integração especial.

Evitar regras frágeis como `Y > 80 = mountain`. Preferir:

- altitude real/normalizada da dimensão;
- referência local de elevação quando necessária;
- exposição ao céu;
- perfil do bioma;
- weather/season.

Uma montanha muito alta criada pelo worldgen deve emergir naturalmente como ambiente mais frio/exposto.

### 4.3 Dimensões

O calendário pode existir globalmente, mas o efeito ambiental da season é dependente da dimensão.

**Overworld**

- seasons completas;
- clima/temperatura;
- neve/gelo quando climaticamente válidos;
- ecologia/vegetação;
- apresentação sazonal.

**Nether / Incendium**

- não aplicar primavera/verão/outono/inverno terrestre de forma literal;
- perfil térmico próprio e geralmente extremo;
- biomas modulam calor/risco;
- efeitos de calor e exposição podem existir sem snow/freeze terrestre.

**End / Nullscape**

- não aplicar seasons terrestres por padrão;
- perfil ambiental próprio/alienígena;
- custom biomes devem continuar classificáveis por fallback;
- nenhuma neve terrestre automática apenas porque o calendário está no inverno.

**Checkpoint de implementação LW-125 / primeiro slice:** `EnvironmentalDimensionPolicy` formaliza esse contrato sem nomes de mods. `World.Environment.NORMAL` mapeia para OVERWORLD com fatores 1.0 de season/day-night/weather e permite ecologia/superfícies congeladas terrestres. NETHER, THE_END e CUSTOM usam profile conservador com esses fatores em 0 e flags terrestres desabilitadas.

`AmbientTemperaturePolicy` aceita esse profile como modulação. Em dimensões não terrestres, a temperatura coordenada do Paper continua sendo a base — inclusive propriedades de biomas customizados — mas o calendário não injeta `+6/-6 °C`, a hora não cria ciclo térmico terrestre e storm não produz resfriamento terrestre. Overloads legados continuam equivalentes ao profile OVERWORLD.

`PaperEnvironmentalDimensionMapper` mapeia exclusivamente `World.Environment`; ele não lê nome do mundo, namespace do biome nem detecta Terralith/Tectonic/Incendium/Nullscape.

## 5. Separar temperatura ambiente de estado térmico do jogador

O modelo futuro deve separar dois conceitos que hoje estão parcialmente combinados no `ApparentTemperaturePolicy`.

### 5.1 AmbientTemperature

Representa o ambiente e governa ecologia/físicas como neve e gelo.

Entradas candidatas:

- temperatura/humidade do local;
- season;
- anomalia climática;
- hora/fase do dia;
- weather;
- altitude;
- exposição ao céu;
- dimensão;
- perfil ambiental do bioma/região.

Roupa do jogador nunca pode impedir um rio de congelar.

### 5.2 PlayerThermalState

Representa como o jogador está termicamente.

Entradas candidatas:

- temperatura ambiente;
- água/molhado;
- abrigo e exposição;
- sprint/atividade;
- armadura/isolamento;
- fontes de calor/frio próximas;
- contato direto com lava/fogo/água;
- acumulação/recuperação ao longo do tempo.

Isso permite:

```text
AmbientTemperature = -12 °C
PlayerThermalState  = recovering
porque o jogador entrou em abrigo aquecido.
```

A atual temperatura aparente do HUD continua sendo contrato existente até uma fatia específica migrá-la com testes e compatibilidade.

**Checkpoint de implementação LW-122 / primeiro slice:** `AmbientTemperature` e `AmbientTemperaturePolicy` foram introduzidos como domínio puro. A calibração já validada de Paper temperature + season foi movida para essa política; `ApparentTemperaturePolicy` passa a compor sobre o ambiente e mantém `WATER/FIRE/LAVA` apenas como exposição pessoal legada. O resolver climático Paper pode consultar `ambientTemperatureAt(Block)`. Nenhuma matemática corporal, wetness, armadura, abrigo ou fonte próxima foi implementada neste slice.

### 5.3 Princípio Vanilla+ do sistema térmico

A simulação interna pode ser relativamente sofisticada, mas a leitura pelo jogador deve continuar intuitiva e baseada em comportamentos vanilla.

Princípio:

> **Condição ambiental cria o cenário; exposição determina a velocidade da troca; equipamento altera resistência; tempo produz consequência.**

Fluxo conceitual:

```text
Ambient Conditions
        |
        v
Exposure / Microclimate
        |
        v
Thermal Exchange Rate
        |
        v
Equipment / Insulation / Wetness / Activity
        |
        v
BodyThermalLoad over time
        |
        v
PlayerThermalState
        |
        v
Feedback / Gameplay consequence
```

O jogador não precisa conhecer valores internos como `BodyThermalLoad=-0.61`. Ele deve conseguir concluir, pela própria experiência de Minecraft, que está molhado, exposto ao vento, no alto de uma montanha e precisa de abrigo/calor.

Itens e comportamentos vanilla devem ganhar significado ambiental antes de Living World inventar equipamentos próprios.

### 5.4 BodyThermalLoad e inércia

`PlayerThermalState` não deve ser recalculado como uma temperatura corporal instantânea derivada apenas de somas de graus.

Direção proposta:

```text
delta =
    environmentalExchange
  + localHeatSources
  + activityHeat
  - waterCooling
  - windCooling
  +/- equipment modifiers

BodyThermalLoad += delta * elapsedTime
```

O nome final/tipo matemático ainda será decidido no LW-122. A propriedade obrigatória é **inércia**:

**Checkpoint de implementação LW-122 / segundo slice:** o estado corporal agora usa `PlayerThermalState`, um índice interno normalizado de `-1` (déficit térmico máximo) a `+1` (excesso térmico máximo), com `0` como equilíbrio. Esse índice não é Celsius nem diagnóstico fisiológico. `ThermalExchangeRate` representa a taxa líquida de alteração por segundo e `PlayerThermalPolicy` integra essa taxa pelo tempo decorrido, saturando nos limites. As faixas `EXTREME_COLD` até `EXTREME_HEAT` são derivadas por `PlayerThermalThresholds`, cujos defaults são tuning inicial e podem evoluir sem mudar a unidade ambiental.

A integração é independente da frequência de atualização: dividir o mesmo intervalo em passos menores produz o mesmo resultado para uma taxa constante. Nenhum scheduler Paper, dano, efeito visual, wetness, armadura ou fonte térmica local foi ligado neste slice.

- entrar numa casa quente não recupera o jogador instantaneamente;
- teleportar para um pico congelado muda o ambiente imediatamente, mas o corpo esfria ao longo do tempo;
- teleportar de uma região congelante para o Nether não causa sobreaquecimento instantâneo;
- exposições breves podem ser toleradas;
- exposições severas prolongadas acumulam consequências.

Bandas futuras podem incluir `EXTREME_COLD`, `FREEZING`, `VERY_COLD`, `COLD`, `COOL`, `COMFORTABLE`, `WARM`, `HOT`, `OVERHEATING` e `EXTREME_HEAT`. Os thresholds e taxas são tuning a validar, não constantes fechadas.

### 5.5 Armadura, isolamento e cobertura

Armadura não adiciona graus diretamente; ela altera troca térmica. Evitar `Leather Chestplate = +5 °C`. Preferir que isolamento/retenção modulem `heatLoss` e `heatGain` antes de atualizar o estado corporal.

**Checkpoint de implementação LW-122 / décimo slice:** armadura agora possui domínio térmico próprio. `ArmorSlot` define cobertura aproximada por slot; `ArmorThermalCatalog` fornece perfis configuráveis de `insulation` e `waterResistance` por material; `ArmorThermalLoadout` combina no máximo uma peça por slot; `ArmorInsulationPolicy` calcula isolamento efetivo ponderado pela cobertura e reduz `AirThermalTransferFactor`.

O isolamento atua nas duas direções: em frio reduz perda de calor, mas também reduz a dissipação quando o corpo está quente. Isso evita o erro de transformar armadura em bônus de °C. O conjunto completo tem mais efeito que uma única peça, e os defaults dão ao couro mais isolamento que chainmail enquanto Netherite retém mais que Diamond. Esses valores são identidade/tuning Vanilla+ inicial, não física real nem ranking universal de “melhor armadura”.

`waterResistance` já existe no perfil do material, mas **ainda não modifica WetnessRate** neste slice; isso será ligado separadamente para evitar misturar isolamento seco com comportamento de armadura molhada.

**Checkpoint de implementação LW-122 / décimo-segundo slice:** `ArmorWetnessPolicy` agora usa `waterResistance` ponderada por cobertura para reduzir apenas taxas positivas de `WetnessRate`. Nenhuma armadura fica impermeável; secagem negativa não é alterada neste estágio. Em paralelo, `ArmorInsulationPolicy` passou a receber `WetnessState` e degrada parte do isolamento efetivo quando o jogador está molhado, sem zerar toda proteção.

Cenários provam que conjunto de couro retarda saturação na água sem impedi-la e que couro saturado isola menos que couro seco. Turtle shell oferece maior resistência local à água que capacete de ferro pelos defaults atuais. Retenção de água/secagem específica por material continua adiada até playtest justificar esse nível de detalhe.

Direção de gameplay:

- couro deve ganhar utilidade real de isolamento no frio;
- chainmail tende a baixo isolamento/alta ventilação;
- metais não devem receber bônus térmicos arbitrários apenas para diferenciar materiais;
- Netherite pode ter alta retenção térmica, ajudando no frio e dificultando dissipação em calor extremo;
- Diamond deve continuar forte sem obrigatoriamente ser a melhor solução térmica;
- cobertura importa: chest/legs tendem a pesar mais que head/feet;
- um conjunto completo deve ser mais relevante que uma única peça.

Essas identidades são hipóteses de balanceamento a validar no LW-122/LW-123, não valores finais. O modelo pode expor propriedades como `Insulation`, `HeatRetention` e `WaterResistance` sem exigir que todo material possua valores diferentes.

### 5.6 Encantamentos

Encantamentos só devem influenciar temperatura quando sua semântica vanilla sustentar essa expectativa:

- `Fire Protection` pode reduzir transferência térmica extrema de fogo/lava;
- `Frost Walker` pode futuramente reduzir exposição fria de contato nos pés, se playtest justificar;
- `Protection` genérico permanece neutro por padrão;
- não transformar encantamentos em uma tabela oculta de bônus de temperatura;
- evitar punição/bônus duplicado com mecânicas vanilla já existentes.

### 5.7 Wetness

Molhamento deve ser estado acumulável separado da temperatura ambiente: `Wetness = 0.0 .. 1.0`.

**Checkpoint de implementação LW-122 / terceiro slice:** `WetnessState` modela umidade acumulada em `[0, 1]`, `WetnessRate` representa molhar/secar por segundo e `WetnessPolicy` integra a taxa pelo tempo decorrido. Assim como o estado corporal, a integração é independente da frequência de atualização e satura de forma segura em seco/completamente molhado. Chuva, submersão, abrigo, calor e equipamento ainda não foram ligados; eles serão apenas produtores/modificadores dessa taxa.

Fontes candidatas: chuva, contato parcial com água, natação/submersão e outras precipitações apenas quando fizer sentido. Recuperação vem de tempo, abrigo, calor, fogueira e condições secas.

Wetness aumenta troca térmica com ambiente frio; não significa automaticamente `-X °C`. Uma tempestade quente de verão não deve causar hipotermia só porque o jogador está molhado.

**Checkpoint de implementação LW-122 / sétimo slice:** `AirThermalExchangePolicy` agora converte `AmbientTemperature` + `WetnessState` + `AirThermalTransferFactor` em `ThermalExchangeRate`. O ar possui um equilíbrio térmico-alvo próprio; wetness multiplica a velocidade da troca, não altera a temperatura ambiente. `AirThermalTransferFactor` tem baseline explícito e será o ponto de entrada futuro para vento/abrigo sem acoplar a policy a Paper ou geometria.

Um cenário integrado cobre o caso “saiu da água no frio”: dois jogadores em `0 °C`, um seco e um saturado, partem do mesmo estado; após o mesmo intervalo, o molhado possui déficit térmico maior. Isso valida a persistência da consequência da água fora da submersão sem recorrer ao antigo `inWater = -4 °C`.

Armadura pode modificar velocidade de molhamento/secagem ou eficiência de isolamento quando molhada, mas a primeira implementação deve permanecer simples. Exemplo de direção: `dry + leather -> bom isolamento`; `wet + leather -> ainda ajuda, mas menos`.

### 5.8 Água e profundidade

Água deve ser uma exposição térmica de alta transferência, não apenas um modificador fixo. Estados úteis: contato leve/pés, submersão parcial, natação e submersão completa. Quanto maior a fração corporal submersa, maior a troca térmica.

**Checkpoint de implementação LW-122 / quarto slice:** `WaterExposure` representa `submergedFraction` em `[0,1]` e profundidade abaixo da superfície em blocos. `WaterExposurePolicy` converte isso em duas saídas independentes: `WetnessRate` e `WaterThermalTransferFactor`. Submersão maior aumenta os dois; profundidade aumenta somente a intensidade da transferência e possui bônus bounded/saturado. Isso evita o erro de fazer um jogador já totalmente submerso “molhar mais rápido” só por estar mais fundo.

`WaterThermalTransferFactor` é adimensional e não possui sinal. O sistema **ainda não assume que toda água é fria**: a direção de ganho/perda térmica depende do futuro `WaterTemperature`. Os valores atuais de `WaterExposureSettings` são tuning inicial de gameplay, não constantes físicas.

Também deve existir um conceito de `WaterTemperature` derivado do ambiente, dimensão, season e profundidade quando necessário. Não precisa reproduzir oceanografia real; precisa ser coerente e previsível.

**Checkpoint de implementação LW-122 / quinto slice:** `WaterTemperature` agora existe como valor próprio em °C e `WaterTemperaturePolicy` deriva uma aproximação bounded a partir de `AmbientTemperature` + profundidade. A superfície acompanha apenas parte do ambiente; profundidade reduz essa influência e aproxima a água de uma temperatura estável configurável. O intervalo líquido também é bounded, evitando água líquida arbitrariamente abaixo do mínimo configurado. Não há estado persistente por lago/oceano nem tabela de nomes de bioma.

Os defaults (`8 °C` de referência estável, acoplamento superficial parcial e estabilização gradual até uma profundidade de referência) são tuning inicial Vanilla+, não afirmações de física universal. Eles devem permanecer configuráveis/testáveis e podem ser refinados quando dimensão/custom-biome profiles forem implementados.

**Checkpoint de implementação LW-122 / sexto slice:** `WaterThermalExchangePolicy` transforma `WaterTemperature` + `WaterThermalTransferFactor` + estado corporal atual em `ThermalExchangeRate`. Em vez de subtrair graus, a água define um equilíbrio térmico-alvo normalizado e o corpo converge gradualmente para ele com taxa bounded. Isso permite comportamento fisicamente coerente de gameplay: água fria resfria um jogador confortável, mas pode aquecer alguém ainda mais frio que o equilíbrio daquela água; contato parcial reage mais lentamente que submersão total.

Um cenário integrado de domínio cobre queda em água superficial a `0 °C`: em 10 s de submersão total o jogador satura wetness progressivamente e alcança a banda `FREEZING`, sem salto instantâneo. Esses tempos/thresholds continuam tuning inicial e não autorizam dano automático.

Profundidade deve importar gradualmente: perto da superfície há pouca penalidade extra; em água profunda o ambiente pode ficar mais severo; em grande profundidade entram frio, baixa luz e isolamento do clima superficial. Não adicionar dano de pressão automaticamente.

### 5.9 Água sob gelo

Água abaixo de gelo deve ser um dos cenários frios mais perigosos: `cold ambient + near-freezing water + full submersion -> high thermal loss + high wetness`.

Ao sair, o jogador permanece molhado e continua perdendo calor até secar/ser aquecido. Cair em lago congelado deve ser mais perigoso que caminhar na neve, mas ainda oferecer tempo de reação.

### 5.10 Fontes locais de calor e frio

Fontes térmicas devem alterar a **taxa de troca térmica local**, não reescrever `AmbientTemperature`.

**Checkpoint de implementação LW-122 / décimo-primeiro slice:** `LocalHeatSourcePolicy` recebe apenas observações já resolvidas (`LocalHeatExposure`) e converte fonte + distância + exposição + linha de visão em `ThermalExchangeRate` positivo com falloff quadrático e cap global de stacking. O domínio não procura blocos; um resolver Paper futuro deverá fornecer uma lista bounded/cached.

O catálogo Vanilla+ inicial mantém papéis distintos: torch/lantern dão conforto pequeno, furnace/smoker/blast furnace moderado, fire/campfire forte e lava extremo. Cenários provam que uma tocha ajuda mas não resolve frio extremo ao ar livre, fogueira + abrigo pode inverter a perda e recuperar o jogador, e exposição prolongada à lava acumula `OVERHEATING` mesmo sem contato direto.

Modelo conceitual: `sourceFlux = sourcePower * distanceFalloff * exposureFactor * optionalLineOfSightFactor`.

Ordem conceitual, sujeita a tuning: torch/lantern = pequeno conforto; furnace/smoker aceso = moderado; fire = forte; campfire = forte e apropriado para recuperação; lava = extremo e perigoso.

Uma tocha não deve resolver sozinha um inverno extremo. Fogueira deve ter papel claro de sobrevivência. Lava aquece sem contato e exposição prolongada pode levar a sobreaquecimento.

Fontes frias candidatas: powder snow, ice, packed ice, blue ice e água extremamente fria. Não criar scans por raio a cada tick; fontes locais exigem desenho cached/indexed/bounded.

### 5.11 Atividade física

Atividade pode gerar calor metabólico moderado: parado = baseline; walking = pequeno efeito; sprinting = maior geração; swimming = atividade + alta troca com água; climbing = pequeno/moderado; Elytra/high-speed exposure pode aumentar convecção quando aplicável.

**Checkpoint de implementação LW-122 / nono slice:** `PlayerActivity` separa esforço metabólico de exposição ambiental. `ActivityThermalPolicy` gera apenas a contribuição positiva acima do baseline para `WALKING`, `SPRINTING`, `SWIMMING`, `CLIMBING` e `GLIDING`; `RESTING` adiciona zero. Elytra possui baixo calor metabólico aqui — seu resfriamento por velocidade continuará pertencendo a `WindExposure`.

Cenários de domínio garantem que sprint reduz a perda líquida em frio extremo sem invertê-la, swimming não supera o resfriamento de água a `0 °C`, e sprint aumenta ganho térmico em ambiente quente. Portanto atividade ajuda, mas não substitui abrigo/equipamento.

Sprint nunca deve substituir abrigo/fogueira em frio extremo. Em calor, atividade + armadura de alta retenção pode acelerar sobreaquecimento.

### 5.12 Vento, exposição e altitude

`WindExposure` pode derivar de weather/storm, sky exposure, altitude, shelter e deslocamento/voo quando relevante. Em frio, `cold + wet + wind` aumenta muito o resfriamento; em calor, fluxo de ar pode ajudar dissipação.

**Checkpoint de implementação LW-122 / oitavo slice:** `WindExposure` e `ShelterFactor` existem como valores normalizados independentes. `AirTransferPolicy` combina os dois em `AirThermalTransferFactor` bounded: vento aumenta transferência; abrigo reduz. O domínio continua sem saber o que é uma casa, caverna ou pico — adapters futuros apenas precisam fornecer fatores observáveis.

Um cenário integrado compara dois jogadores molhados sob o mesmo `AmbientTemperature`: um em pico ventoso/exposto e outro fortemente abrigado/calmo. Após o mesmo intervalo, o exposto acumula déficit térmico maior. Isso valida a direção de gameplay antes de qualquer probe Paper.

Isso é especialmente importante para Terralith + Tectonic: picos devem ser severos pela combinação de altitude, exposição, clima e vento, não por um único threshold de Y.

### 5.13 Abrigo sem detectar semanticamente uma casa

Living World não deve tentar provar que uma construção é uma casa. Preferir `SkyExposure`, `WindExposure`, `RainExposure`, `ShelterFactor` e fontes térmicas locais.

O `ShelterFactor` implementado neste estágio é apenas domínio. A futura resolução Paper deve usar probes bounded/cached de exposição, sem flood-fill de interiores, sem reconhecimento de paredes/portas e sem scans recorrentes de chunks.

Isso permite cabanas, cavernas, castelos, buracos improvisados e construções customizadas. Materiais de parede com propriedades térmicas específicas ficam adiados até haver evidência de que melhoram gameplay sem tornar o cálculo caro.

### 5.14 Cavernas

Profundidade subterrânea não deve equivaler automaticamente a mais quente. Cavernas profundas devem tender a condições mais estáveis: menor influência de hora/weather, pouco vento e influência sazonal superficial reduzida quando apropriado. Lava/fogo local podem criar microclima quente sem alterar a temperatura base de toda a caverna.

### 5.15 Nether / Incendium e calor

No Nether, a experiência térmica deve ser sobre **acúmulo de calor**, não dano ambiental constante obrigatório. `hot biome + nearby lava + sprint/activity + high heat-retention armor` pode evoluir de `WARM` para `HOT` e `OVERHEATING`.

O sistema deve criar pressão e escolhas, não transformar toda permanência no Nether em punição inevitável. Perfis de Incendium refinam intensidade, mas o fallback dimensional continua funcional.

### 5.16 Powder Snow

Powder Snow já possui congelamento vanilla. `PlayerThermalState` pode reconhecer o contato como exposição fria extrema, enquanto dano/overlay vanilla continuam sob responsabilidade principal da mecânica vanilla. Objetivo: coerência, não double punishment.

### 5.17 Comida e fome

Evitar tabela de itens como `steak = +4 °C` e `apple = -2 °C`. Alimentação/saturação pode influenciar capacidade de recuperação/manutenção térmica; fome severa pode prejudicar retenção/recuperação. Stews/sopas só ganham papel especial se playtest justificar.

### 5.18 Sono

Sono cruza tempo lógico rapidamente, mas `PlayerThermalState` é estado corporal de curto prazo. Não integrar automaticamente horas fictícias de troca térmica apenas porque o calendário pulou a noite. Dormir em local seguro pode oferecer recuperação controlada com semântica própria e testada.

### 5.19 Morte, respawn e teleporte

No respawn, redefinir estados corporais de curto prazo como wetness, thermal load e exposições transitórias. Clima do mundo permanece independente.

Em teleporte, `AmbientTemperature` muda imediatamente para o destino; `PlayerThermalState` mantém inércia e começa a reagir ao novo ambiente; caches espaciais do local anterior devem ser invalidados.

### 5.20 Veículos e estados vanilla

O sistema deve respeitar contexto real: barco sobre água não equivale a submersão; chuva ainda pode molhar quando aplicável; cair do barco inicia exposição à água; mount/vehicle não deve ser tratado como contato térmico com o bloco abaixo sem evidência real.

### 5.21 Consequências progressivas

Para preservar Vanilla+: frio/calor leve -> feedback; exposição relevante -> desconforto; exposição severa prolongada -> penalidades leves; extremo prolongado -> dano.

Não aplicar dano/Slowness forte imediatamente ao entrar em um bioma. O jogador deve receber sinais claros e tempo para reagir, aprender e utilizar abrigo/equipamento/ambiente.

### 5.22 Performance do microclima

Não executar `for each player every tick -> scan radius N for lava/torches/ice`.

Direção: cache de contexto térmico por jogador; invalidação por movimento significativo/região, weather/season, equipamento/wetness e block place/break de fontes térmicas; índice bounded de fontes por chunk/região quando necessário; atualização coarse para integração temporal sem re-scan completo; jitter para pulsos visuais; chunks carregados por padrão, sem force-load para temperatura.

O desenho final deve medir se um índice de fontes é realmente mais barato que sampling bounded antes de adotá-lo.

### 5.23 Composição térmica instantânea

**Checkpoint de implementação LW-122 / décimo-terceiro slice:** `ThermalExchangeContext` agrega somente valores já resolvidos para uma avaliação instantânea: estado corporal atual, wetness, ambiente, água, atividade, vento, abrigo, armadura e observações bounded de calor local. `ThermalExchangeComposer` orquestra as policies existentes e retorna `ThermalExchangeResolution` com breakdown explícito de `airRate`, `waterRate`, `activityRate`, `localHeatRate`, `netRate`, `wetnessRate`, fator final do ar e `WaterTemperature` opcional.

O compositor **não mantém estado, não avança relógio, não acessa Paper e não procura blocos**. Em submersão total, a parcela de troca com o ar vai a zero; em submersão parcial, apenas a fração corporal ainda exposta ao ar contribui. Isso preserva ownership e evita transformar a composição em um `EnvironmentManager` global.

### 5.24 Integração temporal bounded

**Checkpoint de implementação LW-122 / décimo-quarto slice:** `PlayerThermalSimulation` integra `ThermalExchangeComposer` + `PlayerThermalPolicy` + `WetnessPolicy` em subpassos internos bounded. O default usa passo máximo de 1 segundo e catch-up máximo de 5 segundos por chamada; tempo excedente é explicitamente reportado como descartado em `ThermalSimulationResult`.

O contexto ambiental permanece constante durante uma chamada, mas `PlayerThermalState` e `WetnessState` são realimentados a cada subpasso. Testes provam equivalência entre uma janela de 10 s e dez janelas de 1 s quando o catch-up permite todo o intervalo. Isso evita acoplar a simulação à frequência exata de um futuro scheduler e evita trabalho proporcional a pausas longas do servidor.

### 5.25 Estado corporal runtime

**Checkpoint de implementação LW-122 / décimo-quinto slice:** `PlayerThermalSnapshot` agrupa apenas `PlayerThermalState + WetnessState`. `PlayerThermalStateStore` define um contrato mínimo por UUID e `InMemoryPlayerThermalStateStore` fornece a implementação process-local thread-safe.

O estado é **efêmero por design** neste estágio: não usa PDC, não sobrevive restart e pode ser removido explicitamente em quit/respawn. Isso combina com a decisão de que morte/respawn limpam estado corporal de curto prazo e evita confundir memória ambiental persistente com a condição física de um corpo anterior.

### 5.26 Orquestração runtime sem Paper

**Checkpoint de implementação LW-122 / décimo-sexto slice:** `ThermalEnvironmentContext` separa observações ambientais do snapshot corporal. `PlayerThermalRuntimeService` executa o fluxo `load snapshot -> combinar com ambiente -> simulate -> save snapshot` por UUID, usando o store efêmero e a simulação bounded.

Essa fronteira é propositalmente independente de Bukkit/Paper. Futuros resolvers Paper devem apenas produzir `ThermalEnvironmentContext`; não podem recalcular matemática térmica nem manipular diretamente o estado corporal. `reset(UUID)` fornece o contrato necessário para quit/respawn sem persistir hipotermia/wetness de um corpo anterior.

### 5.27 Primeiros resolvers Paper + runtime coarse

**Checkpoint de implementação LW-122 / décimo-sétimo slice:** a fronteira Paper agora produz `ThermalEnvironmentContext` real sem duplicar matemática térmica. `PaperPlayerActivityResolver` usa flags/velocidade do próprio jogador; `PaperArmorThermalResolver` lê apenas os quatro slots e cobre os materiais vanilla atuais, incluindo **copper armor** no Paper 26.3; `PaperWaterExposureResolver` usa quatro sondas corporais e só busca superfície quando totalmente submerso.

A profundidade da água é bounded em 32 blocos e usa probing exponencial + refinamento binário, evitando scan linear de coluna. Waterlogged blocks e bubble columns contam como água; lava não.

`PaperWindShelterResolver` usa skylight bruto, weather, altitude relativa ao sea level e velocidade horizontal. Abrigo é derivado de exposição ao céu; nenhuma construção é classificada semanticamente como “casa”.

`PaperLocalHeatSourceResolver` usa **raios fixos bounded**, nunca cubo/volume: no máximo 96 block probes e 8 fontes por avaliação, somente em chunks já carregados, interrompendo raios em blocos oclusivos. Torch/lantern/furnace/smoker/blast furnace/fire/campfire/lava/magma entram quando aplicável; furnace/campfire apagados não aquecem.

`PaperThermalRuntimeModule` é owner do pulse runtime. Default: habilitado, 20 ticks (1 s), trabalho proporcional aos jogadores do mundo climático. O primeiro pulse apenas inicializa relógio; pulsos seguintes usam elapsed real + simulação bounded. Quit/respawn limpam snapshot; mudança de mundo reinicia somente o relógio para não fabricar catch-up quando o jogador retornar. Ainda não existem dano, HUD corporal ou efeitos visuais.

**Smoke Paper parcial do slice 17:** o artefato de `47241fa` foi iniciado em Paper 26.3 build 133 via configuração `Run` do IntelliJ. O servidor carregou/enabled `LivingWorld 0.1.0-SNAPSHOT` e alcançou `Done` sem exceções do runtime térmico. A primeira tentativa encontrou corretamente um `session.lock` pertencente à instância Paper antiga; essa instância foi encerrada via SIGTERM, o lock liberado e o novo processo iniciou normalmente. O cliente MCPFabric permaneceu desconectado após o restart, portanto esse smoke comprova lifecycle/startup, **não** ainda execução com jogador conectado.

### 5.28 Diagnóstico térmico somente leitura

**Checkpoint de implementação LW-122 / décimo-oitavo slice:** `ThermalRuntimeReadout` + `PaperThermalRuntimeReadoutProvider` expõem diagnóstico bounded sem avançar estado. O provider lê o snapshot já acumulado do jogador, resolve o ambiente atual e calcula um breakdown instantâneo via `ThermalExchangeComposer`.

`/lw thermal` apresenta: faixa corporal + carga + wetness; temperatura ambiente + atividade + contato/profundidade da água; vento + abrigo + quantidade de peças de armadura + fontes locais de calor; e as contribuições por segundo de ar, água, atividade, calor local e taxa líquida.

O comando é **observacional**: não salva novo snapshot, não altera relógio, não aplica dano/efeitos e não duplica matemática. Ele existe para smoke/tuning antes de qualquer feedback corporal player-facing.

**Smoke conectado do slice 18:** com o jogador real `zVaporius` conectado no Paper 26.3, `/lw thermal` mostrou estado acumulado e breakdown coerente. Em superfície/outono: 13 °C, parado, seco, sem armadura/fontes, vento ~52%, taxa líquida negativa pequena e carga corporal já em `COOL`. Em imersão total a ~5 blocos: wetness chegou a 100%, a parcela de ar zerou e a água passou a dominar a troca. Em movimento dentro d'água, o resolver mudou para `SWIMMING` e a contribuição metabólica apareceu separadamente. Quatro peças de couro foram detectadas e reduziram a magnitude da troca com o ar, inclusive durante recuperação de um corpo frio. Uma fixture temporária de `MAGMA_BLOCK` em bloco previamente confirmado como `AIR` foi detectada como fonte local e depois restaurada para `AIR`.

O smoke também revelou que o runtime não estava secando wetness fora da água: `WaterExposureEffect.none()` fornecia taxa neutra e nenhuma policy ambiental de secagem estava conectada.

### 5.29 Wetness ambiental, chuva e secagem

**Checkpoint de implementação LW-122 / décimo-nono slice:** `PrecipitationExposure` entrou no contexto ambiental e `WetnessEnvironmentPolicy` passou a compor água + precipitação − secagem. Fora de água/chuva, wetness seca gradualmente; vento, temperatura ambiente mais alta e calor local aceleram a secagem. Precipitação exposta adiciona wetness, e precipitação em ambiente ≤ 0 °C molha mais devagar pelos defaults iniciais.

O Paper deriva precipitação como `world.hasStorm() × skyExposure`, reutilizando a mesma observação O(1) de skylight usada por vento/abrigo. Jogador completamente abrigado recebe exposição de precipitação 0. O diagnóstico `/lw thermal` agora também mostra exposição à precipitação e a taxa líquida de wetness por segundo.

Armadura continua reduzindo apenas taxas positivas de wetness; secagem negativa não é alterada por `ArmorWetnessPolicy`. Isso preserva a separação entre resistência à entrada de água e futura retenção/secagem específica por material, que segue adiada até playtest justificar.

### 5.30 Precipitação por propriedades + contato térmico direto

**Checkpoint de implementação LW-122 / vigésimo slice:** precipitação Paper continua derivada sem tabela de nomes. Uma tempestade global só produz `PrecipitationExposure` quando o bloco local possui `humidity > 0`; a umidade funciona como gate binário, não como intensidade. Isso evita molhar deserto/savana seca por simples `world.hasStorm()` e mantém fallback para biomas customizados baseado em propriedade observável.

`DirectThermalExposure` adiciona um canal separado para `NONE / FIRE / LAVA / POWDER_SNOW`. `PaperDirectThermalExposureResolver` usa apenas flags O(1) do próprio Player. Prioridade: lava > powder snow > água suprimindo fogo residual > fogo. Água continua pertencendo ao modelo próprio de submersão/profundidade.

`DirectThermalExposurePolicy` transforma contato direto em `ThermalExchangeRate`: fogo aquece, lava aquece mais severamente e powder snow esfria. O canal entra separadamente no `ThermalExchangeResolution` e no `/lw thermal`, portanto não é confundido com fontes locais por distância.

Importante: esse canal **não aplica dano, potion effects nem freeze ticks**. Powder snow continua sob a mecânica vanilla para overlay/dano de congelamento; Living World apenas incorpora a exposição ao estado corporal. Assim `visualFreeze != thermalDamage` continua preservado.

### 5.31 AmbientTemperature: altitude nativa + hora/weather expostos

**Checkpoint de implementação LW-122 / vigésimo-primeiro slice:** foi verificado no bytecode do Paper 26.3 que `CraftWorld#getTemperature(x,y,z)` delega a `Biome#getTemperature(BlockPos, seaLevel)`. Portanto altitude/posição vertical **já fazem parte do escalar térmico coordenado** que Living World consome. O plugin não aplica um segundo lapse rate, evitando double-counting em montanhas Tectonic.

`AmbientTemperatureFactors` adiciona somente fatores que o escalar coordenado não representa como estado temporal do mundo: `skyExposure`, hora do dia e `AmbientWeather`. O default Vanilla+ usa uma curva diária suave: céu aberto aquece até +2,5 °C perto do meio-dia e resfria até -3,5 °C perto da meia-noite. Chuva acrescenta -1,5 °C e thunder -2,5 °C. Todos esses modificadores são multiplicados por exposição ao céu; interior/caverna com sky exposure zero mantém a temperatura base mais estável.

Weather local também respeita `humidity > 0`; uma tempestade global não cria resfriamento de chuva invisível em bioma seco. Isso continua property-based e funciona para namespaces customizados sem tabela obrigatória.

`PaperLocalClimateResolver` agora oferece dois contratos: `baseAmbientTemperatureAt` preserva a calibração histórica biome + season; `ambientTemperatureAt` adiciona hora/weather para o runtime corporal. `/lw climate` continua usando o base por compatibilidade explícita, enquanto `/lw thermal` usa o ambiente refinado. Assim a migração do readout legado não acontece silenciosamente.

### 5.32 Temperatura sazonal gradual

**Checkpoint de implementação LW-122 / vigésimo-segundo slice:** `SeasonCycle` agora expõe progresso normalizado da estação com base no mesmo `CalendarDate`, `monthsPerSeason` e `daysPerMonth` já usados pelo calendário. `PaperCalendarModule` fornece esse progresso via `CalendarView.currentSeasonProgress()`; nenhum segundo relógio ou calendário paralelo foi criado.

Para o runtime térmico, cada estação mantém o anchor já calibrado no **meio** da estação. O início usa a média entre os anchors da estação anterior e atual; o fim usa a média entre atual e próxima. As metades são interpoladas com `smoothstep`, produzindo Early/Mid/Late contínuos e derivada suave.

Exemplo com os anchors atuais: Primavera começa entre Inverno (-6 °C) e Primavera (+2 °C), atinge +2 °C no meio e termina entre Primavera (+2 °C) e Verão (+6 °C). O fim da Primavera e o início do Verão usam exatamente o mesmo boundary, portanto não existe degrau de temperatura na troca do rótulo.

O overload legado de `AmbientTemperaturePolicy.temperature(paperTemperature, season)` continua usando o anchor discreto e `/lw climate` permanece compatível. Apenas o caminho runtime de `ambientTemperatureAt` consome progresso sazonal gradual.

### 5.33 LW-123 — feedback térmico puro

**Checkpoint de implementação LW-123 / primeiro slice:** `ThermalFeedbackPolicy` separa explicitamente duas causas que não devem ser confundidas:

- **cold breath** depende da temperatura do ar ao redor do rosto, não apenas de o corpo já estar frio;
- **frost/cold sensation** depende do `PlayerThermalState` acumulado, mesmo se o jogador acabou de entrar em ambiente mais quente.

`BreathFeedback` retorna intensidade normalizada e uma janela `minimumInterval..maximumInterval` para jitter posterior. O ar mais frio aumenta intensidade e reduz a janela de intervalo. Sprint/caminhada/natação/escalada alteram apenas cadência, não a intensidade visual do vapor. Submersão total suprime breath na boca.

`ThermalFeedbackProfile` também retorna `frostIntensity` progressiva. Frost começa apenas após déficit corporal relevante e cresce até frio severo. A policy é domínio puro e **não escolhe partículas, não agenda tarefas, não usa freeze ticks, não aplica dano e não altera o estado térmico**.

Esse slice preserva o contrato central: `visualFreeze != thermalDamage`. O próximo passo é agendamento bounded/jittered; somente depois entra um adapter Paper de apresentação.

### 5.34 LW-123 — cadência/jitter bounded

**Segundo slice:** `ThermalFeedbackCadencePolicy` converte uma amostra normalizada em um intervalo dentro da janela pedida por `BreathFeedback`. `ThermalFeedbackRuntimeService` mantém apenas countdown por UUID para breath elegível, emite no máximo um pulso por avaliação e nunca tenta recuperar múltiplas emissões após lag/catch-up.

O primeiro profile elegível agenda sem emitir imediatamente, evitando uma parede sincronizada de vapor ao entrar num bioma frio. Profiles mais severos podem reduzir o restante da espera ao novo `maximumInterval`. Quando breath deixa de ser elegível, o countdown é removido imediatamente. `reset` e `clear` garantem cleanup de lifecycle.

`ThermalFeedbackCoordinator` é a ponte entre o runtime térmico coarse e a apresentação. Ele calcula o profile somente quando o runtime ambiental já resolveu o contexto e o cacheia; o pulse visual não repete probes Paper. Jogadores sem breath e sem frost não permanecem observados; countdown de breath só existe enquanto breath está elegível.

### 5.35 LW-123 — cold breath Paper

**Terceiro slice:** `PaperThermalFeedbackModule` roda em pulse visual configurável (default 10 ticks) sobre profiles já cacheados. Ele não lê blocos, clima, armadura ou fontes de calor; apenas avança a cadência e chama um presenter quando há emissão.

`PaperColdBreathPresenter` usa, até smoke visual, o fallback vanilla **provisório** `Particle.CLOUD` próximo à boca/olhos, ligeiramente à frente da direção de visão. Intensidade mapeia para **1..3 partículas**. Cada emissão possui orçamento de **no máximo 16 viewers**; o próprio jogador recebe o efeito e viewers já retornados por `getTrackedBy()` só recebem quando `canSee(subject)`. Jogador invisível não emite, evitando denunciar invisibilidade; vanish por viewer continua respeitando `canSee`.

O módulo ignora SPECTATOR, usa elapsed real por jogador e não produz burst depois de pausa longa. Nenhum efeito desse slice altera `PlayerThermalState`, aplica dano, usa freeze ticks ou adiciona novos probes de mundo.

Frost continua apenas como `frostIntensity` no profile até existir uma apresentação segura que não conflite com HUD/action bar nem com o sistema vanilla de congelamento.

### 5.36 LW-123 — frost visual seguro

**Quarto slice:** `PaperSnowflakeFrostPresenter` transforma `frostIntensity` em feedback visual **self-only** usando `Particle.SNOWFLAKE` próximo à câmera do próprio jogador. Intensidade mapeia para 1..3 partículas por pulse visual; nenhum pacote de frost é enviado para terceiros.

O frost usa o mesmo `PaperThermalFeedbackModule` de 10 ticks e pode existir mesmo quando breath está desligado — por exemplo, corpo ainda muito frio após entrar em abrigo quente. O gate do módulo considera qualquer profile com breath **ou** frost elegível.

Esse fallback não usa `setFreezeTicks`, não altera `maxFreezeTicks`, não aplica dano, não escreve action bar/boss bar e não cria efeitos de potion. Portanto o overlay/dano vanilla de Powder Snow continua totalmente separado do feedback corporal do Living World.

**Refinamento do terceiro slice:** o fallback seguro de frost foi implementado como `PaperSnowflakeFrostPresenter`, self-only, usando `Particle.SNOWFLAKE` com **1..3 partículas** por pulse conforme intensidade. Ele não altera freeze ticks, não envia frost para terceiros e não aplica dano. Um corpo ainda muito frio pode continuar vendo frost ao entrar em ar quente mesmo quando cold breath já foi desabilitado; isso preserva a independência entre ambiente imediato e estado corporal acumulado.

### 5.37 LW-123 — diagnóstico e tuning

**Quinto slice:** `PaperThermalFeedbackSettings` permite desligar cold breath e frost de forma independente sem desligar o runtime corporal. O toggle global de feedback e o período visual continuam separados do `thermal-runtime`.

`ThermalRuntimeReadout` agora inclui o `ThermalFeedbackProfile` calculado de forma read-only a partir do snapshot corporal e do contexto ambiental atual. `/lw thermal` mostra intensidade prevista de breath, janela de cadência e intensidade de frost. O comando não avança countdown visual, não altera body load e não persiste nada.

Isso cria uma superfície objetiva de smoke/tuning: quando houver cliente, será possível comparar o que a policy **pretende** (`/lw thermal`) com o que a apresentação realmente mostra, sem inferir thresholds visualmente.

Com esse slice, LW-123 fica **implementado mas ainda não validado visualmente**. O fechamento depende de smoke real para posição do vapor, densidade/cadência, conforto do frost e ausência de efeitos durante invisibilidade/SPECTATOR/submersão. Ajustes de partículas após esse smoke são tuning, não mudança de ownership.

## 6. Cálculo incremental, cache e invalidação

Não adotar por padrão um loop que recalcula tudo para todos os jogadores a cada segundo.

Preferir:

```text
season mudou?          -> invalida componente sazonal
biome/região mudou?    -> invalida perfil local
weather mudou?         -> invalida componente de weather
faixa de altitude mudou? -> invalida altitude
equipamento mudou?     -> invalida isolamento
wet state mudou?       -> invalida umidade corporal
heat-source context mudou? -> invalida microclima
```

Um pulso coarse e limitado pode existir como rede de segurança, mas não deve substituir ownership/eventos/cache.

Mundo/Bukkit só é acessado em thread segura. Cálculo puro, agregação e preparação podem ser assíncronos quando houver benefício medido; "jogar tudo async" não é estratégia de performance.

## 7. Feedback térmico: o jogador deve sentir o frio

Temperatura não deve ser apenas um número no HUD.

Bandas de experiência propostas:

- `COMFORTABLE`;
- `COOL`;
- `COLD`;
- `VERY_COLD`;
- `FREEZING`;
- `EXTREME_COLD`.

Os limites numéricos ainda são tuning de gameplay, não contrato fechado.

### 7.1 Respiração visível

Em frio suficiente, o jogador deve emitir vapor próximo à boca/olhos.

Objetivo visual:

- emissão pequena;
- origem próxima ao eye location, deslocada levemente na direção do olhar;
- intervalo irregular/jitter para evitar sincronização de dezenas de jogadores;
- densidade crescente com frio;
- não revelar jogadores invisíveis/vanished;
- somente jogadores elegíveis entram no conjunto de atualização.

Partícula vanilla exata (`CLOUD`, `WHITE_ASH`, `SNOWFLAKE` ou composição) será escolhida por smoke visual. Resourcepack pode melhorar o efeito sem tornar-se requisito.

**Checkpoint de implementação LW-123 / primeiro slice:** `ThermalFeedbackPolicy` produz somente um `ThermalFeedbackProfile` abstrato. Respiração visível depende da temperatura do ar, não apenas do corpo: um jogador hipotermicamente frio em ambiente quente continua com frost corporal, mas não exala vapor. A intensidade de breath cresce de forma contínua abaixo do threshold de ar frio; atividade física encurta a faixa de cadência sem aumentar artificialmente a densidade do vapor.

`BreathFeedback` retorna intensidade + intervalo mínimo/máximo, deixando o jitter concreto para uma camada posterior. Submersão total desabilita emissão na boca. Nenhuma partícula foi escolhida neste slice e nenhum scheduler Paper foi criado.

### 7.2 Sensação de congelamento

Frio progressivo deve combinar, quando adequado:

- respiração;
- som/vento;
- feedback visual de frost;
- tremor ou feedback audiovisual sutil;
- penalidades leves em frio severo;
- hipotermia/dano somente em extremo e de forma configurável.

O efeito vanilla de freeze/freeze ticks é candidato para aproveitar linguagem visual nativa, porém exige spike técnico. **Visual freeze e dano térmico devem permanecer conceitos separados**; não aumentar freeze ticks até causar dano acidental apenas para desenhar overlay.

Resourcepack pode fornecer frost/sons próprios. Sem pack, usar feedback vanilla seguro.

`ThermalFeedbackProfile.frostIntensity` deriva exclusivamente do `PlayerThermalState` acumulado e cresce progressivamente em déficit severo. A policy não manipula freeze ticks, não aplica dano e não escolhe overlay; portanto a fronteira `visualFreeze != thermalDamage` já existe antes de qualquer adapter Paper.

### 7.3 Abrigo e recuperação

Entrar em construção/abrigo deve ser perceptível:

```text
exterior frio + vento + molhado
   -> abrigo reduz exposição
   -> fonte de calor local ajuda
   -> PlayerThermalState recupera gradualmente
   -> frost e respiração diminuem
```

Detecção de abrigo e fontes térmicas próximas precisa de desenho bounded/cached; não escanear área ao redor de cada jogador a cada HUD refresh.

## 8. Visual seasonal projection vs ecologia física

Living World deve usar modelo híbrido:

```text
Environmental State
        |
        +--> Visual Projection (client/player)
        |
        +--> Physical Ecology (server/world)
```

### 8.1 Visual Projection

Boa para efeitos sem consequência física:

- foliage/grass tint;
- water/sky/fog;
- atmosfera;
- partículas;
- sons;
- apresentação de chuva/neve;
- assets sazonais.

Uma direção técnica a investigar é usar biomas auxiliares sazonais definidos em datapack/registry e projetados ao cliente sem reescrever a verdade ecológica do servidor. Isso é **spike**, não arquitetura implementada. Validar primeiro compatibilidade Paper 26.3, registry/packets, custo, reconnect e clients vanilla.

Evitar NMS/packets se API pública suficiente existir; se packet-level for necessário, isolar em adapter estreito e documentar razão.

**LW-126 / spike slice 1:** a inspeção da API pública Paper 26.3 não encontrou qualquer equivalente a `Player#sendBiomeChange`. Existem `Player#sendBlockChange(s)` e player weather, mas biome/tint permanece apenas em APIs de mundo/region; `World#setBiome` altera o biome real do servidor e portanto foi rejeitado como solução de apresentação.

`RegistryAccess` expõe `RegistryKey.BIOME` para leitura, porém `RegistryEvents` não oferece BIOME entre os registries graváveis. Portanto criar biomes auxiliares dinamicamente apenas pela API pública do plugin não está comprovado; datapack continua sendo o caminho declarativo candidato para esses entries.

No runtime local Paper 26.3 existe `ClientboundChunksBiomesPacket`, com payload por `ChunkPos + byte[]`, e `CraftPlayer#getHandle().connection` oferece uma rota NMS tecnicamente possível para envio per-player. Isso **não** significa implementação aprovada: o buffer de biome precisa ser construído corretamente, version-specific NMS deve ficar confinado a adapter estreito e nenhuma mutação de biome real pode ser usada como atalho.

Resultado do slice: **API pública insuficiente para foliage/grass/water/sky/fog per-player via biome; NMS packet é viável como hipótese, ainda não implementado**. Até esse adapter ser comprovado, Living World mantém partículas/sons/resourcepack vanilla-safe como fallback e não promete broad seasonal tint.

**LW-126 / spike slice 2:** bytecode do runtime local Paper 26.3 mostra que `ClientboundChunksBiomesPacket.ChunkBiomeData` serializa apenas `LevelChunkSection#getBiomes()` de cada seção. O tamanho é a soma de `getSerializedSize()` desses containers e a escrita é feita sequencialmente em `FriendlyByteBuf`.

`PalettedContainer` expõe `copy()`, `set(x,y,z,value)` e `write(...)`, então o caminho conceitual não precisa clonar blocos, luz ou entidades. `ServerLevel.registryAccess()` + `Registries.BIOME` permitem resolver o `Holder<Biome>` alvo, e `ServerGamePacketListenerImpl#send(Packet)` fornece envio apenas ao jogador desejado.

Algoritmo candidato:

    loaded LevelChunk
      -> for each LevelChunkSection
           -> copy biome PalettedContainer
           -> replace 4x4x4 biome cells with visual Holder<Biome>
           -> serialize copied container
      -> ChunkBiomeData(chunkPos, biomeBytes)
      -> ClientboundChunksBiomesPacket
      -> target player's NMS connection only

Esse desenho preserva a verdade do servidor porque nunca chama `World#setBiome` nem modifica o container real do chunk. Ainda assim permanece **experimental**: depende de NMS version-specific, precisa ser version-gated, bounded a chunks já enviados/carregados e validado visualmente antes de entrar em produção.

### 8.2 Physical Ecology

Se o efeito muda colisão/gameplay, o estado real do servidor deve ser coerente.

Exemplos:

- snow layer física;
- WATER -> ICE caminhável;
- ICE -> WATER no degelo;
- mudanças de flora que realmente alteram blocos.

Não fingir gelo sólido apenas no cliente quando o servidor continua vendo água.

## 9. Inverno físico bounded e lazy

Neve/gelo físicos devem ser aplicados apenas quando climate/policy permitirem e sempre com orçamento explícito.

Fluxo conceitual:

```text
active player / relevant event
  -> ClimateSnapshot / AmbientTemperature
  -> candidate surface
  -> policy
  -> bounded mutation budget
  -> real server block mutation
```

Não:

```text
winter started
  -> scan every loaded chunk
  -> repaint/freeze whole world
```

**LW-127 / slice 1:** o domínio físico agora possui `PhysicalWinterSettings` e `WinterThermalPhasePolicy`. A histerese inicial usa `FREEZING` em <= -1 °C, `THAWING` em >= +2 °C e `HOLDING` entre os thresholds. Esses números são tuning inicial de código, não constantes físicas finais.

`WinterSurfaceOwnershipLedger` registra apenas blocos criados pelo Living World, com limite bounded por chunk. `WinterSurfacePosition` compacta X/Z locais e Y absoluto em um `int`, preservando Y negativo; `WinterSurfaceKind` usa IDs persistentes explícitos (`ICE=1`, `SNOW=2`) em vez de ordinal.

`PaperWinterSurfaceOwnershipStore` persiste pares `packedPosition, kindId` no PDC do próprio chunk. Dados ímpares ou kind desconhecido falham fechado. Esse ownership é requisito para degelo: futuros mutations só poderão remover/reverter superfícies que o Living World comprovar que criou.

Nenhum bloco é alterado neste slice. O próximo owner Paper deve usar apenas chunks já carregados, budget explícito e atividade local; ownership persistente vem antes da primeira mutação.

**LW-127 / slice 2:** `PaperPhysicalWinterModule` introduz a primeira mutação física ativa, ainda restrita a ICE e desligada por default. O owner percorre apenas jogadores online e, a cada 40 ticks, usa no máximo 4 probes por jogador, raio 8 e no máximo 1 mutação por jogador. Antes de qualquer `getHighestBlockAt`, confirma que o chunk alvo já está carregado; não força load nem percorre todos os chunks.

Freeze só ocorre quando `PaperWinterSurfaceTargetResolver` encontra WATER source (`Levelled.level == 0`) exposta ao céu (`lightFromSky == 15`) com bloco acima vazio, a dimensão permite frozen surfaces e `WinterThermalPhasePolicy` está em `FREEZING`. A mutação é `WATER -> ICE`, sem física, seguida de claim persistente no chunk PDC.

Thaw só ocorre para `ICE` cujo `WinterSurfaceOwnershipLedger` confirma `WinterSurfaceKind.ICE`. Em `THAWING` — ou em dimensão que não permite frozen surfaces — a mutação é `ICE -> WATER` e o ownership é removido. ICE natural ou de jogador sem ownership é tratado como `OTHER` e nunca é removido por este módulo.

**LW-127 / SNOW integration:** `PaperWinterSnowMutator` agora roda dentro do mesmo probe/mutation budget de `PaperPhysicalWinterModule`, antes do caminho de ICE. Em `FREEZING` com precipitação, suporte válido pode receber uma camada de `SNOW`; neve já owned pode acumular até o `max-snow-layers` configurado (default 4). Em `THAWING` ou dimensão não-terrestre, somente neve owned perde camadas e o ownership é removido quando a última camada desaparece.

`PaperWinterSnowTargetResolver` trata neve natural/de jogador como `OTHER` e também rejeita `WATER`/`LAVA` como suporte de placement. Assim o módulo não cobre fluidos com snow layer e não assume ownership de neve existente. ICE e SNOW continuam usando ownership persistente separado pelo mesmo ledger do chunk.

Break/place/fade observados pelo módulo limpam ownership na posição afetada para evitar provenance obsoleta em interações normais de gameplay. Physical SNOW continua fora deste slice.

**LW-127 / boundedness regressions:** testes do owner Paper fixam os budgets como contrato executável. Com `maxMutationsPerPlayer=1`, o primeiro freeze encerra os probes restantes daquele jogador; quando nenhum candidato muda, `probesPerPlayer=4` resulta em exatamente quatro consultas de superfície. Se `World#isChunkLoaded` for falso, `getHighestBlockAt` nem é chamado. Adicionar SNOW não autoriza aumentar esses limites automaticamente.

**LW-127 / SNOW foundation:** `PhysicalSnowSettings` define um cap inicial configuracional de domínio de 4 layers (válido entre 1 e 8). `WinterSnowMutationPolicy` fica separado da policy de ICE: somente `FREEZING + precipitação` pode colocar/acumular snow owned; frio seco preserva sem acumular; `THAWING` ou dimensão sem frozen surfaces derrete somente snow owned.

`PaperWinterSnowTargetResolver` usa regras vanilla do próprio Paper para o suporte futuro: snow existente só é tratada como `OWNED_SNOW` se o ledger confirmar ownership; snow natural/jogador falha fechado. Para nova deposição, o bloco acima precisa estar vazio e `Block#canPlace(SNOW)` deve aceitar o placement. Nenhum bloco de snow é mutado neste sub-slice.

A futura integração deve reutilizar o mesmo probe do ICE e o mesmo mutation budget. Não deve existir scheduler separado para neve, nem segundo scan da área por jogador.

Congelamento/degelo deve considerar histerese para impedir oscillation próxima de 0 °C. Valores como congelar abaixo de -2 °C e derreter acima de +2 °C são exemplos de tuning, não decisão final.

O custo deve escalar principalmente com jogadores/áreas ativas e mudanças relevantes, não com tamanho total do mapa.

## 10. Transições sazonais graduais

Visual futuro não deve ser uma troca binária instantânea:

```text
Summer
  -> Late Summer
  -> Early Autumn
  -> Mid Autumn
  -> Late Autumn
  -> Early Winter
```

Usar progresso dentro da season para interpolar apresentação quando tecnicamente viável.

Exemplos de direção:

**Primavera**

- brotação/floração;
- vegetação mais viva;
- partículas leves;
- retorno gradual após dormência.

**Verão**

- maturidade;
- atmosfera quente sutil;
- efeitos visuais reduzidos onde não agregam valor.

**Outono**

- múltiplos tons de copa;
- queda de folhas;
- vegetação entrando em dormência;
- atmosfera mais seca/fria.

**Inverno**

- vegetação dormente/desaturada;
- precipitação/neve onde climate permite;
- superfícies frias;
- rios/lagos congelando fisicamente apenas quando válido;
- respiração/frost para jogadores expostos.

Deserto quente em inverno pode permanecer sem neve/gelo. Season influencia, mas não substitui clima local.

## 11. Performance como contrato

Princípio:

> trabalho ambiental deve ser proporcional à atividade real, com budgets explícitos e mensuráveis.

Cada feature deve declarar limites, por exemplo:

- máximo de probes por ativação;
- máximo de mutações físicas por janela;
- máximo de packets/efeitos por jogador;
- máximo de entries em cache;
- frequência máxima de fallback/coarse update.

Valores concretos serão definidos por implementação/benchmark, não por estética de código.

Técnicas desejadas:

- event-driven antes de polling;
- dirty flags/invalidation;
- caches por jogador/região quando justificados;
- jitter para tarefas visuais periódicas, evitando herd effect;
- work queues bounded para transformações;
- chunks já carregados por padrão; evitar force-load apenas para estética;
- nenhum scan recorrente de mundo/chunks.

## 12. Benchmark obrigatório antes de alegar vantagem

Perfis mínimos futuros:

```text
1 jogador
10 jogadores
25 jogadores
50+ jogadores
```

Medir quando ferramentas permitirem:

- TPS/MSPT;
- tempo da feature/tick ou evento;
- allocations;
- quantidade de probes;
- mutações;
- packets enviados;
- cache hit/miss;
- event invocations;
- backlog/budget saturation.

Comparar baseline sem a feature e com a feature. Não dizer "mais performático que X" sem evidência comparável.

## 13. Próximo bloco — M12.5 Environmental & Thermal Foundation

Ordem proposta e aceita como direção:

1. **Climate profiles e compatibilidade de dimensão/custom biome**
   - manter sampling por coordenada;
   - namespaced/fallback;
   - Overworld/Nether/End com respostas ambientais próprias.
2. **AmbientTemperature**
   - separar temperatura do ambiente da sensação corporal.
3. **PlayerThermalState**
   - exposição, água, atividade, isolamento, abrigo e calor com trabalho bounded.
4. **Thermal Feedback**
   - cold breath;
   - sensação progressiva de congelamento;
   - feedback vanilla/resourcepack com fallback seguro.
5. **Plugin/datapack/resourcepack contracts**
   - schemas/manifests;
   - bootstrap do datapack;
   - resourcepack delivery/status;
   - nenhum motor concorrente.
6. **Worldgen Compatibility Gate**
   - Terralith + Tectonic;
   - Incendium;
   - Nullscape;
   - namespace desconhecido.
7. **Seasonal Visual Projection spike**
   - validar técnica de cores/biomas/packets antes de compromisso.
8. **Bounded Physical Winter**
   - neve real;
   - gelo real;
   - degelo;
   - budgets/histerese.
9. **Performance gate**
   - benchmark e limites antes de expandir flora/efeitos.

M12.5 deve fortalecer a fundação antes de adicionar volume de efeitos. Flora sazonal mais ampla vem depois de a infraestrutura ambiental e de apresentação estar comprovada.

## 14. Não objetivos

- não recriar Terralith/Tectonic/Incendium/Nullscape;
- não exigir Fabric/NeoForge no cliente;
- não transformar datapack em scheduler global;
- não trocar resourcepack inteiro por estação;
- não manter duas fontes de season/climate;
- não aplicar snow/freeze terrestre ao Nether/End por simples calendário;
- não fazer block scans globais;
- não usar fake client blocks sólidos se isso quebrar colisão/verdade de gameplay;
- não otimizar por intuição nem prometer superioridade sem medição;
- não iniciar implementação das etapas futuras apenas porque foram documentadas.

## 15. Critérios arquiteturais de sucesso

Esta direção é considerada preservada quando:

- calendar/season/climate continuam com uma única autoridade;
- `AmbientTemperature` e `PlayerThermalState` são conceitos distintos quando a migração ocorrer;
- custom worldgen participa sem tabela obrigatória de nomes;
- Overworld/Nether/End podem ter respostas ambientais diferentes;
- plugin controla contratos de datapack/resourcepack;
- resourcepack é apresentação e possui fallback quando configurado como opcional;
- efeitos físicos têm verdade server-side;
- efeitos puramente visuais podem usar projeção client-side;
- todo trabalho recorrente é bounded e possui owner;
- performance é medida antes de alegações comparativas.
