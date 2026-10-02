# Living World — Project State

- **Protocol:** Agent Development Protocol v2.2 snapshot in repository
- **Product scope:** `PRODUCT.md` — Foundation v0.1
- **Requirements:** `PRODUCT.md`
- **Task inventory:** `TASKLIST.md`
- **Roadmap:** `ROADMAP.md`
- **Architecture:** `DESIGN.md`
- **Current task:** LW-123 cold-breath three-puff runtime visual smoke; return to LW-127 immediately after this presentation gate closes.
- **Current acceptance:** one breath decision produces exactly three bounded micro-puffs at 0/2/4 ticks, each recomputing the current head-local mouth frame; the sequence advances forward, grows gradually and gains only progressive upward bias, with no positional spread around the head. Existing cadence still owns how often a breath occurs.
- **Current validation:** LW-129 is validated. LW-123 slices 1-7 remain validated, including mouth-anchored white DUST breath and vanilla freeze-overlay/body-shiver frost. Slice 8 is implemented-not-validated: `PaperColdBreathScheduler` schedules 0/2/4 tick callbacks, puff origins advance only 0.00/0.08/0.16 along local forward, DUST size grows 0.8/1.1/1.4 and upward bias increases across the burst. Focused tests cover schedule, origins, growth, progressive ascent and viewer cap; IntelliJ build/lint and Gradle `Build` are green; packaged JAR SHA-256 `ac87efc9e348e2b31f72442b450fe2fdc224415fe5b89fa3bba374ba8a71b813` started healthy on Paper 26.3 build 141. The Fabric client did not reconnect during the bounded smoke window, so no visual claim is made for the final 0/2/4 trajectory yet. The accepted architecture remains Paper API first with no ProtocolLib dependency; NMS stays isolated/version-gated only where public API is insufficient. LW-127 remains implemented-not-validated and LW-128 representative benchmark remains explicitly deferred.
- **Observed repository:** `/home/luigiapcp/IdeaProjects/Living World`
- **Observed branch:** `ecology/m12-thermal-foundation`
- **Observed integration merge commit:** `703528c` — Waystone navigation clarity + Discovery foundation merged locally before the branch rename to `main`
- **Baseline build:** `./gradlew build` succeeded before protocol initialization
- **Implementation:** M0/M1/M2/M3/M4/M5/M6/M7/M8/M9/M10 validated; M11 in progress (LW-110 validated, LW-111/LW-112 runtime closure pending); M12.3 concluído; M12.4 implementado/validado; M13 in progress (LW-130 validated, LW-131 pending); M14 in progress (LW-140 validated, LW-141 integrated, LW-142 partial)
- **Previous validation (M10):** live /lw climate contrast confirmed savanna 39°C Escaldante/Árido versus snowy_plains -1°C Congelante/Equilibrado with ecology percentages matching the existing growth/farmland/fire/frozen-surface policies; runtime log stayed clean and random_tick_speed remained 3
- **Merge:** local `integration/m10-discovery` merged at `703528c` before branch alignment; GitHub remote configured as `origin` and `main` tracks `origin/main`
- **Deploy:** not applicable
- **Blockers:** none for automated task-graph reconciliation. Runtime/client validation remains deferred for LW-122..LW-128 where noted; LW-126 visual proof and LW-128 benchmark data must not be invented
- **Next action:** when the Fabric client is back in-world, perform one short third-person smoke of the LW-123 0/2/4 breath burst and verify forward-first motion plus progressive rise without head spread. If accepted, restore LW-123 to validated and immediately resume the paused LW-127 physical-winter smoke from the preserved fixture. Keep the LW-128 representative benchmark deferred unless explicitly resumed.

## M12.4 — checkpoint local

- M12.3 concluído conforme contexto explícito do usuário; alterações pré-existentes preservadas.
- Implementado: política visual pura (`SeasonalLeafVisualPolicy`), adaptador de movimento com intervalo de 5s (`PaperSeasonalLeavesModule`), nove sondas máximas em chunks carregados, emissão individual e cleanup.
- Validação automatizada: `./gradlew clean test build` aprovado; 152 testes, 0 falhas/erros/ignorados. `git diff --check` aprovado.
- Runtime: validado em runtime real com servidor Paper 26.3 ativo e cliente Fabric conectado via MCPFabric (`http://127.0.0.1:25599/rpc`). Comprovadas as partículas de folhas em Outono (`PALE_OAK_LEAVES`), restrição climática de neve no Inverno (`SNOWFLAKE` ativo em bioma congelante/frio e suprimido em bioma árido/deserto), ausência no Verão (`NONE`), e respeito à chance ecológica e ao intervalo de 5s. A fixture temporária foi totalmente restaurada (`/lwvisualtest restore`: 162 blocos e biomas restaurados).
- Fora do escopo: recoloração de blocos, neve física, flora dinâmica, scanners, tarefas periódicas e novas features.
- M12.4 recebeu aceite formal do usuário.
- Nova direção aceita: M12.5 Environmental & Thermal Foundation, documentada em `ECOLOGY_AND_SEASONS.md`.
- Ambiente-alvo: Terralith + Tectonic (Overworld), Incendium (Nether), Nullscape (End), com fallback para namespaces desconhecidos.
- Arquitetura aceita: plugin é autoridade; datapack é declarativo; resourcepack apresenta; temperatura ambiente e estado térmico do jogador serão conceitos separados; cold breath/frost são feedbacks previstos; neve/gelo físicos só entram com estado server-side e trabalho bounded.
- Refinamento térmico aceito: realismo interno com leitura Vanilla+; armadura altera isolamento/troca térmica em vez de somar graus; wetness, submersão/profundidade, vento, abrigo, atividade, torch/campfire/lava, cavernas, Nether/Incendium, Powder Snow, sono, respawn e teleporte compõem o mesmo modelo com inércia e consequências progressivas.
- A intenção de manter uma branch/frente dedicada à ecologia foi registrada, porém este worktree continua observado em `master`; nenhuma branch foi criada/trocada nesta atualização documental.

### Arquivos desta fatia

- Novos: `features/ecology/domain/SeasonalLeafVisualPolicy.java`, `features/ecology/paper/PaperSeasonalLeavesModule.java` e os respectivos testes (sob os diretórios Java existentes).
- Integração: `src/main/java/dev/signalshards/livingworld/LivingWorldPlugin.java`.
- Continuidade: `AGENTS.md`, `PRODUCT.md`, `DESIGN.md`, `TASKLIST.md`, `ROADMAP.md`, `PROJECT_STATE.md`.
- Arquitetura ambiental especializada: `ECOLOGY_AND_SEASONS.md`.
- Alterações pré-existentes de M11/M12.3 e a documentação ambiental foram consolidadas no checkpoint local `25fbe20` antes do merge, preservando o estado do `master`.
- Não executado: novo `runServer`/smoke visual, pois há servidor anterior ativo e foi observada pressão de RAM/swap no host. Nenhum deploy/restart e nenhuma afirmação de desempenho medido.

## M13/M14 — checkpoint de integração local

- Checkpoint pré-merge do `master`: `25fbe20`.
- Merge local de `integration/m10-discovery`: `703528c`.
- M13 adiciona ordenação nearest-first no mesmo mundo e lore com mundo/coordenadas/distância, sem alterar identidade, acesso ou segurança de viagem.
- M14 adiciona a fundação de Discovery com escopo pessoal/mundo, PDC versionado, idempotência e integração aditiva com interação física de Waystone.
- Numeração importada reconciliada: Waystone navigation = M13/LW-130..131; Discovery foundation = M14/LW-140..142; trilha futura Discovery = M15..M18.
- Gates no `main` integrado: `./gradlew clean test build`, `./gradlew test --rerun-tasks`, 183 testes em 57 suítes, 0 falhas/erros/skips; IntelliJ build e `git diff --check --cached` aprovados.
- Runtime parcial confirmado pelo usuário em 2026-09-27: uma interação física de Waystone exibiu o título `Descoberto`. Isso comprova o caminho de apresentação inicial, mas não encerra sozinho os checks de repetição, rename/travel, clima e destruição/memória.
- Runtime ainda pendente: completar LW-131/LW-142 com jogador real. Validar menu/lore/travel, silêncio na repetição, backfill idempotente, rename/travel intactos, `/lw climate` coexistente e destruição do anchor sem apagar memória de Discovery.
- GitHub sync confirmed: local `main` pushed to `LuigiAPCPereira/Living-World`, upstream set to `origin/main`, and follow-up `git pull --ff-only origin main` reported `Already up to date`. No deploy was executed.
- Reconciliation 2026-09-27: the earlier summary claiming all of M11 validated was too broad. Live MCPFabric confirms LW-110 (`/lw climate` -> `Tendência local: Precipitação`), but no conclusive NIGHT_SKIP/applied-weather message was observed, so LW-111/LW-112 remain open.
- MCPFabric also confirms the integrated runtime sequence around Discovery/Waystones: create, re-interaction, successful travel, destruction and climate readout all occurred in one client history. The user separately confirmed seeing `Descoberto`. The current client bridge did not produce a conclusive synthetic RIGHT_CLICK_BLOCK for additional fixtures, so LW-131/LW-142 are not over-claimed as complete.
