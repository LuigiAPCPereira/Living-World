# Living World — Project State

- **Protocol:** Agent Development Protocol v2.2 snapshot in repository
- **Product scope:** `PRODUCT.md` — Foundation v0.1 + Discovery foundation (M11)
- **Requirements:** `PRODUCT.md`
- **Task inventory:** `TASKLIST.md`
- **Roadmap:** `ROADMAP.md`
- **Architecture:** `DESIGN.md`
- **Current task:** M12.4 — Ecologia visual / folhas sazonais
- **Current acceptance:** efeitos sazonais próximos ao jogador, chance ecológica reutilizada, trabalho limitado por eventos e nenhum bloco alterado
- **Observed repository:** `/home/luigiapcp/IdeaProjects/Living World`
- **Observed branch:** `master`
- **Observed HEAD:** `99adcda` — M10 local climate readability checkpoint before M11 weather-feedback work
- **Baseline build:** `./gradlew build` succeeded before protocol initialization
- **Implementation:** M0/M1/M2/M3/M4/M5/M6/M7/M8/M9/M10 validated; M11 weather readability validated; M12.3 concluído; M12.4 implementado e validado em runtime via Paper 26.3 e MCPFabric
- **Previous validation (M10):** live /lw climate contrast confirmed savanna 39°C Escaldante/Árido versus snowy_plains -1°C Congelante/Equilibrado with ecology percentages matching the existing growth/farmland/fire/frozen-surface policies; runtime log stayed clean and random_tick_speed remained 3
- **Merge:** not integrated / no remote merge observed
- **Deploy:** not applicable
- **Blockers:** none confirmed
- **Next action:** aceite de M12.4 e definição do próximo incremento

## M12.4 — checkpoint local

- M12.3 concluído conforme contexto explícito do usuário; alterações pré-existentes preservadas.
- Implementado: política visual pura (`SeasonalLeafVisualPolicy`), adaptador de movimento com intervalo de 5s (`PaperSeasonalLeavesModule`), nove sondas máximas em chunks carregados, emissão individual e cleanup.
- Validação automatizada: `./gradlew clean test build` aprovado; 152 testes, 0 falhas/erros/ignorados. `git diff --check` aprovado.
- Runtime: validado em runtime real com servidor Paper 26.3 ativo e cliente Fabric conectado via MCPFabric (`http://127.0.0.1:25599/rpc`). Comprovadas as partículas de folhas em Outono (`PALE_OAK_LEAVES`), restrição climática de neve no Inverno (`SNOWFLAKE` ativo em bioma congelante/frio e suprimido em bioma árido/deserto), ausência no Verão (`NONE`), e respeito à chance ecológica e ao intervalo de 5s. A fixture temporária foi totalmente restaurada (`/lwvisualtest restore`: 162 blocos e biomas restaurados).
- Fora do escopo: recoloração de blocos, neve física, flora dinâmica, scanners, tarefas periódicas e novas features.
- Próxima fatia recomendada: avaliar partículas adicionais ou transições graduais antes de ampliar efeitos.

### Arquivos desta fatia

- Novos: `features/ecology/domain/SeasonalLeafVisualPolicy.java`, `features/ecology/paper/PaperSeasonalLeavesModule.java` e os respectivos testes (sob os diretórios Java existentes).
- Integração: `src/main/java/dev/signalshards/livingworld/LivingWorldPlugin.java`.
- Continuidade: `AGENTS.md`, `PRODUCT.md`, `DESIGN.md`, `TASKLIST.md`, `ROADMAP.md`, `PROJECT_STATE.md`.
- Alterações pré-existentes de M11/M12.3 continuam no worktree; não foram atribuídas a esta fatia nem commitadas.
- Não executado: novo `runServer`/smoke visual, pois há servidor anterior ativo e foi observada pressão de RAM/swap no host. Nenhum deploy/restart e nenhuma afirmação de desempenho medido.
