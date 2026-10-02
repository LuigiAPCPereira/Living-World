# Living World — Handoff

Este arquivo é o checkpoint portátil para continuar em um novo chat. A verdade durável continua em `PRODUCT.md`, `DESIGN.md`, `ECOLOGY_AND_SEASONS.md`, `TASKLIST.md`, `ROADMAP.md` e `PROJECT_STATE.md`.

## Repositório / protocolo

- Repositório: `/home/luigiapcp/IdeaProjects/Living World`
- Branch: `ecology/m12-thermal-foundation`
- Upstream: `origin/ecology/m12-thermal-foundation`
- Protocolo: Agent Protocol.
- No novo chat, começar com `skills://plugins/agent-protocol/continuing-development/skill.md`.
- Para runtime, MCPFabric deve começar por `get_status`. Como o bridge roda no lado client, `serverPresent=false` / `playerCount=0` é normal em multiplayer remoto. Desconexão real é indicada por `get_self -> no_client_player`.
- Deep Research só deve ser usado quando houver uma lacuna externa real. Para continuação normal, usar Agent Protocol + repo + `SomenteAnalise/`.
- Não adotar ProtocolLib. Política aceita: Paper API primeiro; NMS apenas em adapters estreitos, version-gated e fail-closed quando a API pública não basta.

## Base publicada atual

Código de produção/tuning publicado:
- `f0e8cd89af3d01f381fbee87bb4e70b95166d9ce` — `feat: usa frost visual vanilla`
- `a5f1c213e3e638aadac54d37b8a950dda855cc18` — `feat: encadeia cold breath em tres puffs`

No encerramento desta sessão, os experimentos rejeitados foram removidos do worktree. A base de código do cold breath voltou exatamente ao commit `a5f1c213`. `SomenteAnalise/`, `bin/` e `logs/` permanecem não rastreados e não devem entrar em commits.

O runtime também foi realinhado para essa base publicada antes do handoff:
- JAR empacotado: SHA-256 `ac87efc9e348e2b31f72442b450fe2fdc224415fe5b89fa3bba374ba8a71b813`
- Paper 26.3 build 141 / Java 25
- processo iniciado com `/tmp/lw-handoff-a5f1c213.jar`
- o experimento CLOUD não ficou ativo no servidor de handoff.

## Missão ativa — LW-123 cold breath tuning

### O que já está sólido

- A origem da boca usa referencial local da cabeça:
  - `MOUTH_FORWARD_BLOCKS = 0.27`
  - `MOUTH_DOWN_BLOCKS = 0.30`
- O burst é bounded e temporal:
  - puff 1 em tick 0
  - puff 2 em tick 2
  - puff 3 em tick 4
- `PaperColdBreathScheduler` é um boundary estreito de scheduler Paper; não adiciona scan ambiental ou estado global.
- A geometria yaw/pitch, limite de viewers, invisibilidade e scheduling têm regressões automatizadas.
- A cadência de respiração continua pertencendo ao thermal feedback; um burst conta como uma respiração, não três métricas separadas.

### Implementação atual no commit a5f1c213

`PaperColdBreathPresenter` usa DUST branco:
- forward origin offsets: `0.00 / 0.08 / 0.16`
- sizes: `0.8 / 1.1 / 1.4`
- speed multipliers: `1.00 / 0.92 / 0.82`
- upward bias: `0.00 / 0.06 / 0.14`
- `count=0` com vetor de velocidade.

Essa versão está **implemented-not-validated** para o visual final de 3 puffs.

### Fonte de inspiração real em SomenteAnalise

O RealisticSeasons decompilado está disponível em:
- `SomenteAnalise/me/casperge/realisticseasons/particle/ParticleManager.java`
- `SomenteAnalise/me/casperge/realisticseasons/particle/ParticleSpawner.java`

O código real do RealisticSeasons faz três respirações e cada `playBreathOnce` usa três DUST/REDSTONE brancos em 0/2/4 ticks, progressivamente mais à frente e maiores. O Living World **não precisa copiar o burst 3x3**; manter 1 respiração = 3 micro-puffs é a direção preferida por custo/clareza.

### Experimentos runtime desta sessão — decisões importantes

1. **DUST temporal do commit a5f1c213**
   - funciona tecnicamente, mas ainda precisava de tuning visual.

2. **DUST espacial temporário, não commitado**
   - puffs em `0.15 / 0.45 / 0.75` bloco à frente;
   - subida em `0.00 / 0.05 / 0.16`;
   - sizes `0.55 / 0.80 / 1.05`;
   - `count=1`, sem depender de velocity.
   - Em terceira pessoa, o assistente inicialmente interpretou que ainda parecia perto do rosto.
   - **Feedback final do usuário corrige isso:** o DUST era a direção melhor, os puffs não estavam necessariamente rente à boca; terceira pessoa dificultava a leitura. O usuário quer manter DUST e deixá-lo **um pouco maior**.

3. **CLOUD temporário, rejeitado**
   - testado com origem única na boca e velocidades aproximadamente:
     - forward `0.10 / 0.085 / 0.065`
     - upward `0.000 / 0.012 / 0.025`
   - **Usuário rejeitou explicitamente:** CLOUD vai para baixo; não usar essa direção no próximo chat.
   - O experimento CLOUD foi descartado do worktree antes deste handoff.

### Próxima ação exata para cold breath

Começar pelo DUST, não CLOUD.

Candidato recomendado para o próximo tuning:
- manter ticks `0/2/4`;
- manter mouth frame atual;
- usar progressão espacial claramente visível, semelhante ao experimento `0.15 / 0.45 / 0.75` forward e `0.00 / 0.05 / 0.16` up;
- aumentar os tamanhos em relação ao experimento `0.55 / 0.80 / 1.05` (por exemplo, partir perto de `0.75 / 1.0 / 1.25` ou testar os sizes atuais `0.8 / 1.1 / 1.4` com os offsets espaciais maiores);
- não depender de velocity para DUST como principal sensação de movimento;
- smoke em terceira pessoa e primeira pessoa; aceitar somente se a sequência parecer sair da boca, avançar e depois subir sem envolver a cabeça.

Não commitar uma nova tentativa visual sem smoke observado.

## Frost / shiver — concluído

`f0e8cd8` substituiu o antigo SNOWFLAKE corporal por apresentação vanilla:
- `PaperVanillaFrostPresenter`;
- overlay de gelo via `freezeTicks` sempre abaixo de `maxFreezeTicks`;
- nunca usa `lockFreezeTicks`;
- não assume ownership se já houver freeze ticks vanilla/externos;
- powdered snow tem prioridade;
- cleanup em aquecimento, quit, respawn, world-change, spectator/death e disable;
- shiver usa apenas `bodyYaw`, amplitude bounded de aproximadamente `1.5°..4°`, sem mexer yaw/pitch da câmera.

Runtime já validou:
- overlay aparece;
- overlay desaparece em frost 0%;
- body shiver fica perceptível em frost ~59–60%;
- câmera permanece estável;
- SNOWFLAKE corporal foi removido.

## HUD térmica — concluído

LW-129 está validado:
- action bar usa o mesmo `ThermalRuntimeReadoutProvider` de `/lw thermal`;
- temperatura, band, tendência e wetness vêm do snapshot canônico;
- coordenadas aparecem apenas ao segurar COMPASS em uma das mãos;
- runtime validou HUD vs `/lw thermal`.

## Próxima missão depois do cold breath — LW-127 physical winter

LW-127 está implementado-not-validated.

### Config runtime intencionalmente ativa

`run/plugins/LivingWorld/config.yml` deve continuar assim enquanto o smoke LW-127 estiver em andamento:

```yaml
language: pt-BR

ecology:
  physical-winter:
    enabled: true
    update-period-ticks: 40
    probes-per-player: 4
    max-mutations-per-player: 1
    radius-blocks: 8
    freeze-at-or-below-celsius: 50.0
    thaw-at-or-above-celsius: 60.0
    max-owned-positions-per-chunk: 256
    max-snow-layers: 4
```

Não restaurar `language: pt-BR` puro até terminar o LW-127.

### Fixture LW-127

Plataforma:
- stone em y=199, x -134..-118, z -152..-136
- pilar de glass em x=-126,z=-144 y=200..203

Water source blocks:
- (-118,200,-144)
- (-134,200,-144)
- (-126,200,-136)
- (-126,200,-152)
- (-118,200,-136)
- (-134,200,-136)
- (-134,200,-152)
- (-118,200,-152)

Posição de teste LW-127:
- `/tp @s -125.5 200 -143.5`

Observado antes da pausa:
- SNOW apareceu nos half-radius probe points;
- métricas chegaram a 123 probes / 8 mutations e depois 425 probes / 11 mutations;
- server ~20 TPS;
- os oito reservoirs preparados continuaram WATER;
- exemplo (-118,200,-144): `water[level=0]`, acima AIR, suporte STONE.

Próxima investigação LW-127:
1. Ler a ordem exata de `buildProbeOffsets` em `PaperPhysicalWinterModule`.
2. Confirmar se os quatro probes por pulse realmente atingem os water sources.
3. Se atingem, diagnosticar o gate `block.getLightFromSky() >= 15` / exposed source-water sem alterar produção apenas para diagnóstico.
4. Provar ICE + SNOW.
5. Depois testar THAW mudando temporariamente thresholds para:
   - freeze <= -100
   - thaw >= -90
6. Provar owned ICE -> WATER e owned SNOW -> AIR/layer reduction.
7. Validar ownership: natural/player surfaces não devem ser destruídas.

### Cleanup final LW-127

Somente após fechar LW-127:
- restaurar `run/plugins/LivingWorld/config.yml` para exatamente `language: pt-BR\n`;
- remover fixture: `/fill -134 199 -152 -118 203 -136 minecraft:air`;
- remover plataforma alta usada para cold-breath smoke: `/fill -128 309 -146 -122 309 -140 minecraft:air`;
- `/weather clear`;
- teleportar posição original: `/tp @s -126.13202213078024 68 -144.05820910475953`;
- restart final para physical-winter voltar a disabled.

## Runtime / build — armadilhas conhecidas

- IntelliJ `build_project` pode compilar sem regenerar `build/libs/Living World-0.1.0-SNAPSHOT.jar`.
- Antes de qualquer smoke de JAR, usar a run configuration `Build` e confirmar que `:jar`/timestamp/hash foram atualizados.
- Paper alvo: 26.3 build 141, Java 25.
- Não usar o fixture antigo `LWVisualSmoke` para LW-126; ele chama `World#setBiome` e altera verdade server-side.
- `run/` é ambiente temporário; não transformar smoke harness em produto.

## LW-126 / LW-128

- LW-126 está validated; não reabrir sem nova tarefa.
- LW-128: instrumentação validada, benchmark representativo **explicitamente adiado pelo usuário**. Não iniciar benchmark sem autorização explícita.

## Restrições para o próximo chat

- Trabalhar em slices pequenos/médios.
- Agent Protocol primeiro.
- Não usar Deep Research por reflexo.
- Não adicionar ProtocolLib.
- Preservar `SomenteAnalise/`, `bin/` e `logs/` fora de commits.
- Não fazer claim de runtime sem observação direta.
- Não misturar cold-breath tuning com LW-127 no mesmo commit.
