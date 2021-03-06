package gameClient;

import api.*;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * This class contains all the algorithms needed to manage the Pokemon game.
 * algorithms like: places agent before starts the game, crate path, and choose the next move.
 * all the function are static.
 */
public class Algo {

    private static Arena _ar;
    private static directed_weighted_graph _graph;
    private static final double EPS = 0.000001;

    /**
     * Chooses the next move of the giving agent,
     * the next move according to the agent's path.
     * if the destination pokemon of this agent already eaten, then the function call createPath.
     *
     * @param game game_service
     * @param a    agent to choose his next move
     */
    static int nextMove(game_service game, Agent a) {
        int id = a.getId();
        if (indexOfPok(_ar.getPokemons(), a.get_curr_fruit()) == -1) {
            createPath(a);
            return -1;
        }

        int next_dest = a.get_path().get(0).getKey();
        a.get_path().remove(0);
        if (a.get_path().isEmpty()) {
            _ar.get_pokemonsWithOwner().remove(a.get_curr_fruit());
        }

        game.chooseNextEdge(id, next_dest);
//        System.out.println("Agent: " + id + ", val: " + a.getValue() + "   turned to node: " + next_dest);
        return next_dest;
    }

    /**
     * Place the agents in the start of the game in near to the pokemons with the highest value.
     *
     * @param num_of_agents in the current game.
     * @param game          game_service
     */
    static void placeAgents(int num_of_agents, game_service game) {
        PriorityQueue<Pokemon> pq = new PriorityQueue<>(new Comparator<>() {
            @Override
            public int compare(Pokemon o1, Pokemon o2) {
                return Double.compare(o2.get_value(), o1.get_value());
            }
        });
        pq.addAll(_ar.getPokemons());

        for (int i = 0; i < num_of_agents && !pq.isEmpty(); i++) {
            game.addAgent(pq.poll().get_edge().getSrc());
            num_of_agents--;
        }
        if (num_of_agents > 0) {
            placeAgentsByDist(num_of_agents, game);
        }
    }

    /**
     * Place the agents in the start of the game, scatter the agents on the graph.
     *
     * @param num_of_agents in the current game.
     * @param game          game_service
     */
    static void placeAgentsByDist(int num_of_agents, game_service game) {
        dw_graph_algorithms ga = new WDGraph_Algo(_graph);
        ga.shortestPathDist(0, 0);

        PriorityQueue<node_data> pq = new PriorityQueue<>(new Comparator<>() {
            @Override
            public int compare(node_data o1, node_data o2) {
                return Double.compare(o1.getWeight(), o2.getWeight());
            }
        });

        for (Pokemon i : _ar.getPokemons()) {
            pq.add(_graph.getNode(i.get_edge().getSrc()));
        }
        int div = pq.size() / num_of_agents;
        for (int i = 0; i < num_of_agents && !pq.isEmpty(); i++) {
            game.addAgent(pq.peek().getKey());
            for (int j = 0; j < div; j++) {
                pq.poll();
            }
        }
    }

    /**
     * Creates the current path of the giving agent, chooses the strategy of creating the path,
     * and calling the mache function.
     *
     * @param a an agent
     */
    synchronized static void createPath(Agent a) {
        if (_ar.getAgents().size() == _ar.getPokemons().size()) {
            createPathByDistance(a);
        } else {
            if (a.get_speed() > 3) {
                createPathByDistance(a);
            } else {
                createPathByValDist(a);
            }
        }
    }

    /**
     * Create path for the giving agent.
     * This method calculates the ratio of the distance to the value of each Pokemon,
     * and returns the shortest path to the Pokemon that gives the best ratio.
     *
     * @param a an agent
     */
    synchronized static void createPathByValDist(Agent a) {
        dw_graph_algorithms ga = new WDGraph_Algo();
        ga.init(_graph);

        ga.shortestPathDist(a.getSrcNode(), a.getSrcNode());
        Pokemon min_pokemon = _ar.getPokemons().get(0);
        double shortest_way = _graph.getNode(min_pokemon.get_edge().getSrc()).getWeight();
        if (shortest_way == 0) {
            shortest_way = EPS;
        }
        double max_ValDivDist = min_pokemon.get_value() / shortest_way;
        for (Pokemon p : _ar.getPokemons()) {
            if (indexOfPok(_ar.get_pokemonsWithOwner(), p) == -1) {
                double p_src_weight = _graph.getNode(p.get_edge().getSrc()).getWeight();
                if (p_src_weight == 0) {
                    p_src_weight = EPS;
                }
                double temp = p.get_value() / p_src_weight;
                if (max_ValDivDist < temp) {
                    max_ValDivDist = temp;
                    min_pokemon = p;
                }
            }
        }
        List<node_data> path = ga.shortestPath(a.getSrcNode(), min_pokemon.get_edge().getSrc());
        path.add(_graph.getNode(min_pokemon.get_edge().getDest()));
        path.remove(0);
        a.set_path(path);

        a.set_curr_fruit(min_pokemon);
        _ar.get_pokemonsWithOwner().add(min_pokemon);
    }

    /**
     * Create path for the giving agent.
     * This method calculates the shortest path (uses {@link WDGraph_Algo}) from a to all the Pokemons,
     * and returns the shortest path to the shortest Pokemon.
     *
     * @param a an agent
     */
    synchronized static void createPathByDistance(Agent a) {
        dw_graph_algorithms ga = new WDGraph_Algo();
        ga.init(_graph);

        Pokemon min_pokemon = _ar.getPokemons().get(0);
        int n = min_pokemon.get_edge().getSrc();
        double shortest_way = ga.shortestPathDist(a.getSrcNode(), n);

        for (Pokemon p : _ar.getPokemons()) {
            if (indexOfPok(_ar.get_pokemonsWithOwner(), p) == -1) {
                edge_data pokemon_edge = p.get_edge();
                int s = pokemon_edge.getSrc();
                double dist_src = ga.shortestPathDist(a.getSrcNode(), s);
                if (dist_src < shortest_way) {
                    shortest_way = dist_src;
                    min_pokemon = p;
                }
            }
        }
        List<node_data> path = ga.shortestPath(a.getSrcNode(), min_pokemon.get_edge().getSrc());
        path.add(_graph.getNode(min_pokemon.get_edge().getDest()));
        path.remove(0);
        a.set_path(path);

        a.set_curr_fruit(min_pokemon);
        _ar.get_pokemonsWithOwner().add(min_pokemon);
    }

    /**
     * Returns the index within the giving Pokemons list of the first occurrence of
     * the specified Pokemon.
     * In either case, if no such character occurs in this List, then {@code -1} is returned.
     *
     * @param arr List of {@link Pokemon}
     * @param pok {@link Pokemon} to search
     * @return the index of the first occurrence of the Pokemon in the List, or -1 if the Pokemon does not occur.
     */
    public static int indexOfPok(List<Pokemon> arr, Pokemon pok) {
        int ans = -1;
        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i).equals(pok)) {
                ans = i;
                break;
            }
        }
        return ans;
    }

    /**
     * Returns time to sleep (milliseconds) util the next call to move().
     * calculates the walk time to the next node, or to the pokemon.
     *
     * @param a         an agent
     * @param next_dest next destination node of a
     * @return the length of time to sleep in milliseconds
     */
    synchronized static long toSleep(Agent a, int next_dest) {
        edge_data edge = _graph.getEdge(a.getSrcNode(), next_dest);

        if (next_dest == -1 || edge == null) {
            return (long) (130 - a.get_speed() * 7);
        }
        node_data node = _graph.getNode(next_dest);

        if (a.get_curr_fruit() != null && !edge.equals(a.get_curr_fruit().get_edge())) {
            // treat a scenario which the curr fruit cannot be found on the edge:

            double way = edge.getWeight() / a.get_speed();
            way *= 1000;
            return (long) way;

        } else if (edge.equals(a.get_curr_fruit().get_edge())) {
            // treat a scenario which the curr fruit on the current edge:

            double way = a.getPos().distance(a.get_curr_fruit().get_pos());
            double way_to_node = a.getPos().distance(node.getLocation());
            way = way / way_to_node;
            way *= edge.getWeight();
            way /= a.get_speed();
            way *= 1000;
            return (long) way;
        }
        return -120;
    }

    public static void set_ar(Arena _ar) {
        Algo._ar = _ar;
    }

    public static void set_graph(directed_weighted_graph _graph) {
        Algo._graph = _graph;
    }
}
