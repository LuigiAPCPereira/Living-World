# Living World — Handoff

Este arquivo é um checkpoint portátil de sessão. A verdade durável continua em
`PRODUCT.md`, `DESIGN.md`, `ECOLOGY_AND_SEASONS.md`, `TASKLIST.md`,
`ROADMAP.md` e `PROJECT_STATE.md`.

## Fronteira exata

- Repositório: `/home/luigiapcp/IdeaProjects/Living World`
- Branch: `ecology/m12-thermal-foundation`
- Base reconciliada no início desta continuação: `7092ed5`
- Checkpoint de instrumentação LW-128: `383e5a1` — `perf: instrumenta gate ambiental LW-128`
- Upstream: `origin/ecology/m12-thermal-foundation`
- O worktree sujo original do LW-128 foi preservado, inspecionado e commitado sem reset/revert.
- Gate do checkpoint de instrumentação: focused tests verdes; IntelliJ build e inspections de produção limpos; `git diff --check` limpo; suíte completa reexecutada sem cache com 479 testes / 155 suítes / 0 falhas/erros/skips.

## Último escopo publicado

LW-127 está implementado-not-validated: ICE + SNOW físicos usam um único owner
bounded, ownership persistente por chunk, histerese térmica, probes apenas em
chunks carregados e proteção fail-closed de superfícies naturais/jogador.
`PROJECT_STATE.md` registra o último gate publicado em 477 testes / 154 suítes,
0 falhas/erros/skips. Smoke runtime/tuning continua pendente.

LW-126 continua aberto apenas como spike visual: não existe NMS de produção.
O caminho `ClientboundChunksBiomesPacket` foi mapeado conceitualmente, mas prova
com cliente real continua pendente.

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

1. Confirmar branch/HEAD/upstream e que `383e5a1`/`efa75a8` estão presentes.
2. Continuar **LW-126** pelo próximo slice automatizável: preparar corretamente a fronteira NMS/versionada da projeção visual sazonal, sem usar reflection improvisada e sem mutar biome server-side.
3. Tratar qualquer mudança de build para NMS como dependência explícita e validada; preservar `LivingWorldPlugin` como composition root pequeno e manter internals confinados a adapter estreito.
4. Validar por compile/test/build/CI o que puder ser provado sem cliente real; manter a prova visual/client-side como `implemented not validated` até smoke posterior.
5. Manter LW-128 na fila de validação adiada: quando retomado, usar workload/baseline reproduzível, começar em 1 jogador, registrar TPS/MSPT + deltas ambientais e só então fazer claims/otimizações.

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
