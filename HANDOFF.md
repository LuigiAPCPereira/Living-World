# Living World — Handoff

Este arquivo é um checkpoint portátil de sessão. A verdade durável continua em
`PRODUCT.md`, `DESIGN.md`, `ECOLOGY_AND_SEASONS.md`, `TASKLIST.md`,
`ROADMAP.md` e `PROJECT_STATE.md`.

## Fronteira exata

- Repositório: `/home/luigiapcp/IdeaProjects/Living World`
- Branch: `ecology/m12-thermal-foundation`
- HEAD publicado antes deste handoff: `60229c3` — `docs: fecha implementacao automatizada do inverno fisico`
- Upstream: `origin/ecology/m12-thermal-foundation`
- Worktree: **sujo e intencional**; não resetar/reverter.
- Estado observado antes do handoff: 19 entradas, 15 modificadas + 4 untracked, 0 staged, 0 conflitos.
- `git diff --check`: limpo.

## Último escopo publicado

LW-127 está implementado-not-validated: ICE + SNOW físicos usam um único owner
bounded, ownership persistente por chunk, histerese térmica, probes apenas em
chunks carregados e proteção fail-closed de superfícies naturais/jogador.
`PROJECT_STATE.md` registra o último gate publicado em 477 testes / 154 suítes,
0 falhas/erros/skips. Smoke runtime/tuning continua pendente.

LW-126 continua aberto apenas como spike visual: não existe NMS de produção.
O caminho `ClientboundChunksBiomesPacket` foi mapeado conceitualmente, mas prova
com cliente real continua pendente.

## Missão atual — LW-128

O worktree já contém uma implementação **não commitada** da fundação do
environmental performance gate. Não considerar esse código validado ainda.

Delta presente:

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

Arquivos untracked observados:

- `src/main/java/dev/signalshards/livingworld/core/status/EnvironmentalMetric.java`
- `src/main/java/dev/signalshards/livingworld/core/status/EnvironmentalPerformanceMetrics.java`
- `src/main/java/dev/signalshards/livingworld/core/status/EnvironmentalPerformanceSnapshot.java`
- `src/test/java/dev/signalshards/livingworld/core/status/EnvironmentalPerformanceMetricsTest.java`

Os demais arquivos LW-128 aparecem modificados no `git status`; inspecionar o
diff real antes de editar.

## Validação conhecida do delta LW-128

- Ainda **não existe gate durável confirmado** para o worktree LW-128.
- Não inferir que os testes passaram só porque o código está presente.
- O handoff observou apenas `git diff --check` limpo e ausência de conflitos.

## Próxima ação executável

1. Ler `AGENTS.md`, `TASKLIST.md`, `PROJECT_STATE.md`,
   `ECOLOGY_AND_SEASONS.md` e este arquivo.
2. Rodar `git status`, `git diff --stat` e inspecionar o delta LW-128.
3. Não resetar/reverter mudanças preexistentes.
4. Rodar primeiro testes focados:
   `EnvironmentalPerformanceMetricsTest`,
   `PaperThermalRuntimeModuleTest`,
   `PaperThermalFeedbackModuleTest`,
   `PaperPhysicalWinterModuleTest`,
   `PaperResourcePackDeliveryModuleTest` e
   `PaperWaystoneCommandTest`.
5. Corrigir apenas regressões reais. Preservar o desenho de uma instância
   compartilhada de métricas no composition root.
6. Executar build IntelliJ, inspections dos arquivos de produção tocados,
   `git diff --check` e então `./gradlew clean test build`.
7. Só depois checkpointar/pushar o slice de instrumentação LW-128.
8. Para o gate de performance propriamente dito, seguir
   `agent-protocol/investigating-performance`: definir workload/baseline antes
   de otimizar ou fazer claims. Medir perfis 1/10/25/50+ jogadores apenas onde o
   tooling realmente permitir e registrar limitações.

## Restrições importantes

- Para repo/runtime, continuar usando IntelliJ IDEA e MCPFabric; MCPFabric deve
  começar por `get_status` quando runtime for necessário.
- Nenhuma claim de performance sem comparação medida.
- Não alterar gameplay como “otimização” escondida.
- Não iniciar NMS do LW-126 enquanto o LW-128 sujo não estiver reconciliado.
- Trabalhar em **blocos pequenos/médios**: o chat anterior apresentou
  `stream recovery polling timed out` / `Error in input stream` em blocos
  grandes. Preferir uma ação principal e 1–2 chamadas curtas por bloco.

## Skills sugeridos no próximo chat

- `agent-protocol/continuing-development`
- `agent-protocol/investigating-performance`
- `agent-protocol/managing-git-delivery` somente quando os gates estiverem verdes
