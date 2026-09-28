# Living World — Project State

- **Protocol:** Agent Development Protocol v2.2 snapshot in repository
- **Product scope:** `PRODUCT.md` — Foundation v0.1
- **Requirements:** `PRODUCT.md` + active feature contract `DEPOSIT_MATCHING_SPEC.md`
- **Task inventory:** `TASKLIST.md`
- **Roadmap:** `ROADMAP.md`
- **Architecture:** `DESIGN.md` + feature-local contracts
- **Current task:** M20 Deposit Matching QoL on GitHub branch `qol/deposit-matching`; pending M13/M14 runtime validation on `main` remains untouched
- **Current acceptance:** LW-200 automated implementation gate is satisfied; LW-201 real-player inventory smoke remains tracked in `RUNTIME_VALIDATION_BACKLOG.md` and may stay deferred while independent work continues
- **Current validation:** M20 code candidate `d78a100` passed GitHub Actions CI #16 with `./gradlew clean test build`; two-axis code/spec review is complete. This validates LW-200's automated slice only. LW-201 remains runtime-pending. Previous `main` evidence remains historical for M13/M14.
- **Observed repository:** `/home/luigiapcp/IdeaProjects/Living World`
- **Observed base branch:** `main`
- **Observed M20 branch base:** `cb8803e` — includes the deferred-runtime validation policy
- **Observed integration merge commit:** `703528c` — Waystone navigation clarity + Discovery foundation merged locally before the branch rename to `main`
- **Baseline build:** `./gradlew build` succeeded before protocol initialization
- **Implementation:** M0/M1/M2/M3/M4/M5/M6/M7/M8/M9/M10 validated; M11 weather readability has runtime debt; M12.3/M12.4 concluded; M13/M14 have runtime debt; M20 LW-200 is automated-validated on its isolated QoL branch and LW-201 is runtime-pending
- **Previous validation (M10):** live /lw climate contrast confirmed savanna 39°C Escaldante/Árido versus snowy_plains -1°C Congelante/Equilibrado with ecology percentages matching the existing growth/farmland/fire/frozen-surface policies; runtime log stayed clean and random_tick_speed remained 3
- **Merge:** M20 is not merged; no merge to `main` is authorized by the current slice
- **Deploy:** not applicable; no runtime deploy executed for M20
- **Blockers:** no implementation blocker confirmed; user is currently unavailable for local real-player runtime validation
- **Next action:** keep LW-201 queued for later real-player smoke; M20 needs no further implementation work unless runtime evidence finds a defect. Continue only with separately authorized work.

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

## M20 — Deposit Matching branch checkpoint

- Explicit user authorization: implement Deposit Matching as the second QoL slice, using Agent Protocol plus current Minecraft/Paper/Java guidance.
- Branch: `qol/deposit-matching`, based on `main` commit `cb8803e`.
- Product/interaction contract: `DEPOSIT_MATCHING_SPEC.md`.
- Trigger: Shift + right-click an empty slot of an open supported vanilla storage inventory while cursor is empty.
- Supported first slice: real chest/double chest, barrel and shulker box. Ender Chest, machines, entity storage, plugin GUIs and nearby-container scanning remain out of scope.
- Source boundary: player main-inventory slots 9..35 only; hotbar/offhand/armor stay untouched.
- Matching: only `ItemStack.isSimilar` categories present in the initial target snapshot; each source is revalidated against live target state before insertion.
- Transfer: deferred from `InventoryClickEvent` to the next server tick; `Inventory.addItem` provides normal partial-stack/empty-slot insertion; unaccepted remainder stays in the original player slot.
- Conservative UX limitation: the gesture requires an empty top-inventory slot. Fully occupied containers keep vanilla behavior even when an existing stack has spare capacity.
- Performance boundary: event-driven only; no polling and no surrounding-container scan.
- Automated validation: LW-200 code candidate `d78a100` passed GitHub Actions CI #16 (`./gradlew clean test build`) after the final behavior refactor; code/spec review found no remaining blocker.
- Runtime validation: LW-201 deferred into `RUNTIME_VALIDATION_BACKLOG.md`; no runtime success is inferred from CI.
