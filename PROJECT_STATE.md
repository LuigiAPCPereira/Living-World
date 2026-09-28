# Living World — Project State

- **Protocol:** Agent Development Protocol v2.2 snapshot in repository
- **Product scope:** `PRODUCT.md` — Foundation v0.1
- **Requirements:** `PRODUCT.md`
- **Task inventory:** `TASKLIST.md`
- **Roadmap:** `ROADMAP.md`
- **Architecture:** `DESIGN.md`
- **Current task:** nenhuma implementação ativa; LW-121 validado, LW-122 é a próxima tarefa elegível
- **Current acceptance:** M12.5 documentado com direção ambiental recuperável, ownership plugin/datapack/resourcepack explícito, compatibilidade worldgen e próximos gates identificados sem iniciar features futuras
- **Observed repository:** `/home/luigiapcp/IdeaProjects/Living World`
- **Observed branch:** `master`
- **Observed HEAD:** `99adcda` — M10 local climate readability checkpoint before M11 weather-feedback work
- **Baseline build:** `./gradlew build` succeeded before protocol initialization
- **Implementation:** M0/M1/M2/M3/M4/M5/M6/M7/M8/M9/M10 validated; M11 weather readability validated; M12.3 concluído; M12.4 implementado e validado em runtime via Paper 26.3 e MCPFabric
- **Previous validation (M10):** live /lw climate contrast confirmed savanna 39°C Escaldante/Árido versus snowy_plains -1°C Congelante/Equilibrado with ecology percentages matching the existing growth/farmland/fire/frozen-surface policies; runtime log stayed clean and random_tick_speed remained 3
- **Merge:** not integrated / no remote merge observed
- **Deploy:** not applicable
- **Blockers:** none confirmed
- **Next action:** iniciar LW-122 somente quando solicitado: especificar matematicamente AmbientTemperature, microclima e PlayerThermalState (inércia corporal, wetness, água/profundidade, vento/abrigo, atividade, armadura/isolamento e fontes térmicas) preservando contratos existentes antes de ampliar efeitos físicos

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
- Alterações pré-existentes de M11/M12.3 continuam no worktree; não foram atribuídas a esta fatia nem commitadas.
- Não executado: novo `runServer`/smoke visual, pois há servidor anterior ativo e foi observada pressão de RAM/swap no host. Nenhum deploy/restart e nenhuma afirmação de desempenho medido.
