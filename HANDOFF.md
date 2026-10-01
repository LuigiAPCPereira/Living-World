# Living World — Handoff

Este arquivo é um checkpoint portátil de sessão. A verdade durável continua em
`PRODUCT.md`, `DESIGN.md`, `ECOLOGY_AND_SEASONS.md`, `TASKLIST.md`,
`ROADMAP.md` e `PROJECT_STATE.md`.

## Fronteira exata

- Repositório: `/home/luigiapcp/IdeaProjects/Living World`
- Branch: `ecology/m12-thermal-foundation`
- Base reconciliada no início desta continuação: `7092ed5`
- Checkpoint de instrumentação LW-128: `383e5a1` — `perf: instrumenta gate ambiental LW-128`
- Retomada após adiamento do benchmark: `8d530ac` — `docs: adia benchmark LW-128 e retoma LW-126`
- Preparação de build NMS LW-126: `a552170` — `build: prepara userdev NMS para LW-126`
- Gate remoto: `9946ac8` — `ci: adiciona gate Gradle Java 25`; run #1 verde em `clean test build`
- Adapter NMS LW-126: `217e791` — `feat: adiciona adapter NMS de biome LW-126`; run #2 verde em `clean test build`
- Upstream: `origin/ecology/m12-thermal-foundation`
- O worktree sujo original do LW-128 foi preservado, inspecionado e commitado sem reset/revert.
- Gate do checkpoint de instrumentação: focused tests verdes; IntelliJ build e inspections de produção limpos; `git diff --check` limpo; suíte completa reexecutada sem cache com 479 testes / 155 suítes / 0 falhas/erros/skips.

## Último escopo publicado

LW-127 está implementado-not-validated: ICE + SNOW físicos usam um único owner
bounded, ownership persistente por chunk, histerese térmica, probes apenas em
chunks carregados e proteção fail-closed de superfícies naturais/jogador.
`PROJECT_STATE.md` registra o último gate publicado em 477 testes / 154 suítes,
0 falhas/erros/skips. Smoke runtime/tuning continua pendente.

LW-126 continua aberto como spike visual: o adapter NMS de produção
`Paper263SeasonalBiomeProjectionAdapter` existe desde `217e791` e está
compile/build-validado. Ele envia `ClientboundChunksBiomesPacket` apenas ao
jogador-alvo usando cópias dos containers de biome, sem `World#setBiome` nem
mutação do chunk real. O aceite restante é o smoke visual/restoration no
cliente; nenhuma aparência client-side foi comprovada ainda.

## LW-126 — preparação NMS

- `a552170` adiciona `io.papermc.paperweight.userdev` `2.0.0-beta.24` e usa `paperweight.paperDevBundle("26.3.build.+")`, substituindo o `compileOnly` principal de `paper-api`.
- O artifact de produção foi explicitado como `MOJANG_PRODUCTION`, coerente com Paper 26.3; a dependência `paper-api` permanece no classpath de testes.
- A decisão segue o caminho suportado pelo Paper para internals/NMS; reflection improvisada e mutação de biome server-side continuam proibidas.
- `9946ac8` adicionou um GitHub Actions mínimo (Java 25, ações fixadas por SHA, `./gradlew clean test build --no-daemon`); o primeiro run fechou verde e validou o build userdev.
- `217e791` adicionou `Paper263SeasonalBiomeProjectionAdapter`: boundary Paper 26.3 que exige main thread, jogador online/mesmo mundo, usa apenas chunk já carregado, resolve o biome alvo no registry, copia os containers 4×4×4 por seção, serializa as cópias e envia `ClientboundChunksBiomesPacket` somente ao jogador-alvo. Não chama `World#setBiome` e não muta o chunk real.
- O run remoto #2 de `clean test build` também fechou verde. Isso valida compilação/compatibilidade automatizada do adapter, **não** a aparência no cliente.
- O smoke visual do packet fica adiado junto das validações locais: quando retomado, usar fixture temporária/dev harness e comprovar tint/retorno ao biome real sem transformar o harness em feature permanente.

## LW-126 — smoke parcial 2026-10-01

- O worktree local foi fast-forward de `efa75a8` para `5bb685a`; alterações rastreadas permaneceram limpas e o diretório `bin/` não rastreado foi preservado.
- IntelliJ build passou em `5bb685a`; Paper 26.3 build 141 iniciou com LivingWorld sem erro.
- O harness antigo `LWVisualSmoke` foi rejeitado para este aceite porque usa `World#setBiome` e portanto altera verdade server-side.
- Um harness temporário **não commitado** em `run/lw126-smoke` foi criado apenas para exercitar `Paper263SeasonalBiomeProjectionAdapter`; ele não chama `World#setBiome`.
- Com zVaporius conectado no Overworld, o harness chamou `projectWholeChunk` no chunk atual com alvo `minecraft:swamp`; resultado observado: `SENT`.
- No mesmo ponto, o biome server-side era `minecraft:savanna` antes e depois do packet, com `unchanged=true`.
- O caminho de restauração executou `World#refreshChunk` no chunk projetado; retorno `true`, e o biome server-side permaneceu savanna com `unchanged=true`.
- A aparência no cliente não pôde ser observada independentemente: `MCPFabric#get_status` falhou com `MCP SSE probe returned 404 from openai.org`. Portanto este smoke **não fecha** LW-126 sozinho.
- O Paper permaneceu ativo após o smoke; o harness temporário deve continuar fora do produto/commit e pode ser removido após o aceite visual.

## Validação runtime adiada — LW-128

A fundação de instrumentação do environmental performance gate foi consolidada
e validada no commit `383e5a1`. Por decisão explícita do usuário em 2026-09-28,
o benchmark local/runtime fica **adiado para retomada posterior** e não bloqueia
a continuidade automatizada da frente ecológica. Nenhuma claim de performance
é permitida até essa medição ser executada.

Instrumentação consolidada:

- novos `EnvironmentalMetric`, `EnvironmentalPerformanceMetrics` e
  `EnvironmentalPerformanceSnapshot`;
- contadores cumulativos thread-safe via `LongAdder`;
- métricas atuais:
  - `THERMAL_PLAYER_UPDATES`
  - `THERMAL_UPDATE_NANOS`
  - `WINTER_PROBES`
  - `WINTER_MUTATIONS`
  - `WINTER_UPDATE_NANOS`
  - `BREATH_PRESENTATIONS`
  - `FROST_PRESENTATIONS`
  - `RESOURCE_PACK_REQUESTS`
  - `RESOURCE_PACK_STATUS_EVENTS`
- um único `EnvironmentalPerformanceMetrics` é criado no composition root e
  injetado em thermal runtime, thermal feedback, physical winter e resourcepack
  delivery/status;
- `LivingWorldStatusSnapshot` carrega o snapshot ambiental;
- `/lw status` ganhou duas linhas i18n com trabalho ambiental e outputs;
- testes existentes desses módulos/comando foram adaptados;
- novo `EnvironmentalPerformanceMetricsTest` cobre cumulatividade, zeros e
  rejeição de incremento negativo.

Arquivos introduzidos pelo checkpoint:

- `src/main/java/dev/signalshards/livingworld/core/status/EnvironmentalMetric.java`
- `src/main/java/dev/signalshards/livingworld/core/status/EnvironmentalPerformanceMetrics.java`
- `src/main/java/dev/signalshards/livingworld/core/status/EnvironmentalPerformanceSnapshot.java`
- `src/test/java/dev/signalshards/livingworld/core/status/EnvironmentalPerformanceMetricsTest.java`

Os adapters/runtime/status e testes relacionados foram atualizados no mesmo
commit, preservando uma única instância compartilhada de métricas no composition root.

## Validação conhecida do LW-128

- A fundação de instrumentação está **validada e commitada** em `383e5a1`.
- O gate focado incluiu `EnvironmentalPerformanceMetricsTest`, `PaperThermalRuntimeModuleTest`, `PaperThermalFeedbackModuleTest`, `PaperPhysicalWinterModuleTest`, `PaperResourcePackDeliveryModuleTest` e `PaperWaystoneCommandTest`; `PaperResourcePackStatusModuleTest` também foi ampliado para verificar ownership da métrica de status.
- IntelliJ build passou; inspections dos 11 arquivos de produção tocados vieram sem warnings/erros.
- `git diff --check` passou.
- `./gradlew clean test build` passou; como a suíte completa veio do cache, `./gradlew test --rerun-tasks` foi executado depois e passou com 479 testes / 155 suítes / 0 falhas/erros/skips.
- Isso valida a **instrumentação**, não o gate de performance final. Nenhum workload representativo/baseline comparativo foi medido ainda.

## Tentativa de medição — 2026-09-28

- MCPFabric foi consultado primeiro, conforme protocolo, e respondeu no lado client com `serverPresent=false`, `playerCount=0`; portanto não existe perfil real de 1 jogador disponível nesta sessão.
- A run configuration `Run` do IntelliJ é `runServer`. Ela tentou iniciar Paper 26.3 build 135 com o jar atual, mas abortou porque `run/world/session.lock` já estava ocupado.
- A porta 25565 pertence a um Paper 26.3 build 133 iniciado muitas horas antes do checkpoint `383e5a1`; esse processo carregou o plugin antes da instrumentação atual e não é evidência válida para o gate LW-128.
- O servidor antigo foi preservado: não houve kill/restart destrutivo, justamente porque o cliente não estava conectado e reiniciar sozinho não desbloquearia a medição.
- Nenhum TPS/MSPT/counter delta foi registrado como benchmark. Servidor vazio ou runtime pré-instrumentação não substituem o workload aceito.

## Próxima ação executável

1. Obter a confirmação visual que falta para o smoke LW-126 já executado: a projeção `minecraft:swamp` deve ter sido perceptível no cliente e o refresh subsequente deve ter restaurado a apresentação original.
2. Se for necessário repetir com observação independente, começar por `MCPFabric#get_status` quando o bridge voltar e reutilizar o mesmo harness bounded; não criar scheduler/policy de produção para contornar a indisponibilidade do bridge.
3. Não ampliar LW-126 com scheduler/política/biomas inventados antes desse aceite visual.
4. Manter LW-128 na fila de validação adiada: quando retomado, usar workload/baseline reproduzível, começar em 1 jogador, registrar TPS/MSPT + deltas ambientais e só então fazer claims/otimizações.

## Restrições importantes

- Para repo/runtime, continuar usando IntelliJ IDEA e MCPFabric; MCPFabric deve
  começar por `get_status` quando runtime for necessário.
- Nenhuma claim de performance sem comparação medida.
- Não alterar gameplay como “otimização” escondida.
- A antiga restrição de não iniciar NMS enquanto o LW-128 estivesse sujo está satisfeita: a instrumentação foi reconciliada, validada e publicada. O benchmark runtime continua adiado, separado da implementação.
- Trabalhar em **blocos pequenos/médios**: o chat anterior apresentou
  `stream recovery polling timed out` / `Error in input stream` em blocos
  grandes. Preferir uma ação principal e 1–2 chamadas curtas por bloco.

## Skills sugeridos no próximo chat

- `agent-protocol/continuing-development`
- `agent-protocol/investigating-performance`
- `agent-protocol/managing-git-delivery` somente quando os gates estiverem verdes
