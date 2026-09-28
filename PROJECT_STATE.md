# Living World — Project State

- **Protocol:** Agent Development Protocol v2.2 snapshot in repository
- **Product scope:** `PRODUCT.md` — Foundation v0.1
- **Requirements:** `PRODUCT.md` + active feature contract `CONTAINER_SORT_SPEC.md`
- **Task inventory:** `TASKLIST.md`
- **Roadmap:** `ROADMAP.md`
- **Architecture:** `DESIGN.md`
- **Current task:** M21 Container Sort QoL on GitHub branch `qol/container-sort`; M20 Deposit Matching remains on its separate branch and mainline M13/M14 runtime debt is untouched
- **Current acceptance:** LW-210 must pass the exact-branch `./gradlew clean test build` gate plus two-axis scope/engineering review; LW-211 then remains a deferred real-player storage smoke
- **Current validation:** M21 implementation and pure regressions are present. The first CI exposed an invalid Bukkit-dependent unit-test seam and an intermediate adapter/planner mismatch; both were corrected. Exact current-HEAD automated validation is still pending and no runtime success is inferred. Historical M13/M14 evidence remains unchanged.
- **Observed repository:** `/home/luigiapcp/IdeaProjects/Living World`
- **Observed branch:** `qol/container-sort` based directly on `main` commit `cb8803e`
- **Observed integration merge commit:** `703528c` — Waystone navigation clarity + Discovery foundation merged locally before the branch rename to `main`
- **Baseline build:** `./gradlew build` succeeded before protocol initialization
- **Implementation:** M0/M1/M2/M3/M4/M5/M6/M7/M8/M9/M10 validated; M11 weather readability validated; M12.3 concluído; M12.4 implementado/validado; M13 LW-130 validated by integrated-main gates; M14 LW-140/LW-141 implemented with runtime smoke pending
- **Previous validation (M10):** live /lw climate contrast confirmed savanna 39°C Escaldante/Árido versus snowy_plains -1°C Congelante/Equilibrado with ecology percentages matching the existing growth/farmland/fire/frozen-surface policies; runtime log stayed clean and random_tick_speed remained 3
- **Merge:** M21 is not merged; no merge to `main` is authorized by this slice
- **Deploy:** none for M21
- **Blockers:** no product/implementation blocker confirmed; automated exact-HEAD gate and real-player smoke remain evidence tasks
- **Next action:** complete the M21 automated gate/review, keep LW-211 queued for later real-player smoke, then proceed only with separately authorized work

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


## M21 — Container Sort branch checkpoint

- Explicit user authorization: implement Container Sort as the next QoL candidate after community research favored inventory/storage friction reduction.
- Branch: `qol/container-sort`, based directly on `main` so it does not stack on unmerged Deposit Matching work.
- Contract: `CONTAINER_SORT_SPEC.md`.
- Gesture: Shift + left-click an empty slot in the open top storage inventory with empty cursor; Paper must report `InventoryAction.NOTHING`.
- Targets: physical chest/double chest, barrel and placed shulker only. Machines, Ender Chest, entity storage and plugin GUIs are excluded.
- Concurrency: initiating player must be sole viewer; physical target identity and an exact storage snapshot are revalidated one tick later before writing.
- Layout: exact `ItemStack.isSimilar` variants consolidate using their own `getMaxStackSize()`; groups are stably ordered by material namespaced key; same-material metadata variants preserve first-seen relative order rather than receiving hidden classification.
- Player boundary: no main inventory, hotbar, offhand, armor or cursor mutation.
- Full-container limitation: the gesture intentionally requires an empty top slot so occupied Shift + left-click remains vanilla.
- Architecture: pure runtime-independent planner + Paper adapter; no generic storage framework extracted yet.
- Performance/lifecycle: event-driven, one already-open bounded inventory, one deferred owned task, no polling/persistence/nearby scan.
- Validation debt: exact-HEAD automated gate is pending; LW-211 records the real-player matrix. No merge/deploy/runtime claim.
