# Where does an object get created?

Migrations tend to import a DI framework into the shell "because the new code
uses one", and then fight two containers. The rule here is to pick the
**smallest mechanism that owns the object's lifetime**, in this order:

```mermaid
flowchart TD
    Q1{"Does it hold state that must outlive one screen?"}
    Q1 -- no --> A["Construct it where it's used<br/>(e.g. DeepLinkRouter in DeepLinkActivity)"]
    Q1 -- yes --> Q2{"Exactly one per process?"}
    Q2 -- yes --> B["Composition root: a lazy val on the Application<br/>(sessionStore, routes, flowResults, flutterEngines)"]
    Q2 -- "no, one per engine or flow" --> C["Created by the owner of that lifetime<br/>(RealFlutterEngineHost builds handlers per engine)"]
    B --> Q3{"Is the graph now big enough that<br/>wiring by hand is the bug source?"}
    Q3 -- no --> D["Stay with constructors"]
    Q3 -- yes --> E["Introduce a container, on one side only,<br/>and bridge it explicitly at the composition root"]
```

Applied in this repo:

- **Per use**: `DeepLinkRouter`, `ReadinessPipeline` and its gates, and
  `SessionBridge`. They're cheap and stateless, and constructing them at the
  call site keeps their dependencies visible.
- **Per process** (`LegacyShellApplication`): the `MirroredSessionStore` (both
  schemas behind it), `MigrationRoutes`, the `FlowResultBus` and the engine
  host. These must be shared, because two stores in different phases or two
  result buses would split state.
- **Per engine**: the channel handlers. Each engine gets its own set, because a
  flow's handler carries that flow's id ([ADR-0009](adr/0009-flows-return-typed-results.md)).
- **Flutter side**: one `HostPort`, chosen in `main()` by `HOST_MODE` and passed
  down by constructor. Screens receive callbacks, not the port.

No container is used, because the graph is a dozen objects. The decision point
that would change that is in the diagram: when hand wiring itself causes bugs,
not when the object count feels large. Even then the container stays on one
side. The two runtimes meet only through the contract
([ADR-0011](adr/0011-one-versioned-bridge-contract.md)), never through shared
DI.
