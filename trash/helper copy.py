import string
import random
import os

class Node():
    def __init__(self, name, label):
        self.name = name
        self.label = label
        
class DOTGenerator():
            
    DEFAULT_MIN_ID_LENGTH = 3
    
    def __init__(self, directory:str):
        self.directory = directory
    
    def generate_id(self, length:int = DEFAULT_MIN_ID_LENGTH, options:str = "a") -> str:
        if length < self.DEFAULT_MIN_ID_LENGTH:
            raise Exception(f"minimum ID length is {self.DEFAULT_MIN_ID_LENGTH}")

        letters = string.ascii_letters
        underscores = "_"
        numbers = string.digits

        char_set = letters
        id_components = []
        id_components.append(random.choice(letters))

        if "a" in options:
            id_components.append(random.choice(underscores))
            id_components.append(random.choice(numbers))
            char_set += underscores + numbers
        else:
            if "_" in options:
                id_components.append(random.choice(underscores))
                char_set += underscores
            if "n" in options:
                id_components.append(random.choice(numbers))
                char_set += numbers

        while len(id_components) < length:
            id_components.append(random.choice(char_set))

        random.shuffle(id_components)

        if id_components[0] in numbers:
            for i in range(1, len(id_components)):
                if id_components[i] not in numbers:
                    id_components[0], id_components[i] = id_components[i], id_components[0]
                    break

        ID = ''.join(id_components)
        return ID
    
    def generate_file(self, filename:str="", graph:dict[str, dict:[str, str]]=None, graph_options:str="d", id_options:str="a", edge_options:str="l", with_answers:bool=False) -> None:
        node_options = "10: num of nodes, l: random labels, a: all, _: underscores, n: numbers"
        edge_options = "10: num of edges, l: random labels, w: random weights"
        graph_options = "n: random name, d: directed, u: undirected"
        
        labels = dict()
        weights = dict()
        nodes = set()
        edges = set()
        edges_with_attrs = set()
        
        if graph:
            nodes = set(graph["nodes"])
            
            if any('label' in graph['nodes'][node] for node in graph['nodes']):
                labels = {node: attrs['label'] for node, attrs in graph['nodes'].items()}
            
            for from_node, to_node in graph["edges"]:
                edge = f'{from_node} -> {to_node}'
                edges.add(edge)
                
                if len(graph['nodes'][to_node]) != 0:
                    attrs = []
                    
                    if 'label' in graph['nodes'][to_node]:
                        attrs += [f'label="{labels[to_node]}"']
                
                    if 'weight' in graph['nodes'][to_node]:
                        weight = random.randint(0, num_nodes)
                        attrs += [f'weight="{weight}"']
                        
                        weights[edge] = weight
                        
                    edge += f" {attrs}"
                
                edges_with_attrs.add(edge.replace("'", ""))
                
            edge_str = "\n".join([f'\t{edge};' for edge in edges_with_attrs])
        else:    
            nodes_str = ''.join(filter(str.isdigit, graph_options))
            if nodes_str:
                num_nodes = int(nodes_str)
            else:
                num_nodes = random.randint(1, 10)
                
            nodes = set()
            while len(nodes) < num_nodes:
                node = self.generate_id(options=id_options)
                nodes.add(node)
                
                if 'l' in graph_options:
                    label = "".join(random.choice(string.ascii_letters).capitalize() for _ in range(3))
                    labels[node] = label
                
            edges_with_attrs = set()
            edges = set()
            while len(edges_with_attrs) < num_nodes:
                to_node = random.choice(list(nodes))
                from_node = random.choice(list(nodes))
                edge = f'{from_node} -> {to_node}'
                edges.add(edge)
                
                if 'l' in graph_options or 'w' in graph_options:
                    attrs = []
                    
                    if 'l' in graph_options:
                        attrs += [f'label="{labels[to_node]}"']
                
                    if 'w' in graph_options:
                        weight = random.randint(0, num_nodes)
                        attrs += [f'weight="{weight}"']
                        
                        weights[edge] = weight
                        
                    edge += f" {attrs}"
                
                edges_with_attrs.add(edge.replace("'", ""))
                
            edge_str = "\n".join([f'\t{edge};' for edge in edges_with_attrs])
            
        dot_content =  f'''{"digraph" if "d" in graph_options else "graph"} {{\n{edge_str}\n}}'''     
        
        if not filename:
            filename = f'{graph_options}-{id_options}'
            
        with open(f'{os.path.join(self.directory, filename)}.dot', "w") as file:
            file.write(dot_content)
            
        if with_answers:
            answers_content = f"""nodes={nodes}\nedges={edges}\nlabels={labels}\nweights={weights}""".replace("'", "").replace("\\", "")
            with open(f'{os.path.join(self.directory, filename)}.dot.txt', "w") as file:
                file.write(answers_content)


    def generate_empty_digraph(self):
        self.generate_file(graph_options="d0", with_answers=True)

    def generate_large_digraph(self):
        self.generate_file( graph_options=f"d21", id_options="a", with_answers=True)

    def generate_digraph_with_letters(self, n:int=7):
        self.generate_file( graph_options=f"d{n}", id_options="l", with_answers=True)

    def generate_digraph_with_numbers(self, n:int=7):
        self.generate_file( graph_options=f"d{n}", id_options="n", with_answers=True)

    def generate_digraph_with_underscores(self, n:int=7):
        self.generate_file( graph_options=f"d{n}", id_options="_", with_answers=True)

    def generate_digraph_with_labels(self, n:int=7):
        self.generate_file( graph_options=f"d{n}lw", id_options="a", with_answers=True)
      
           
if __name__ == "__main__":
    tmp_path = "../src/test/resources/DOT"
    if not os.path.exists(tmp_path): os.mkdir(tmp_path)
    dot = DOTGenerator(tmp_path)
    dot.generate_empty_digraph()
    dot.generate_large_digraph()
    dot.generate_digraph_with_letters(n=10)
    dot.generate_digraph_with_numbers(n=10)
    dot.generate_digraph_with_underscores(n=10)
    dot.generate_digraph_with_labels(n=12)
    
    threeNodesWithLabelsDirected = {
        'nodes': {
            'a': {
                'label': 'apple',
            },
            'b': {
                'label': 'banana',
            },
            'c': {
                'label': 'coconut',
            },
        },
        'edges': [
            ('a', 'b'),
            ('b', 'b'),
            ('b', 'c'),
            ('c', 'a'),
            ('a', 'a')
        ]
    }
    
    threeNodesDirected = {
        'nodes': {
            'a': {},
            'b': {},
            'c': {},
        },
        'edges': [
            ('a', 'b'),
            ('b', 'b'),
            ('b', 'c'),
            ('c', 'a'),
            ('a', 'a')
        ]
    }
    
    nodesA_1_A_2_A_3 = {
        'nodes': {
            'A_1': {},
            'A_2': {},
            'A_3': {},
        },
        'edges': [
            ('A_1', 'A_2'),
            ('A_3', 'A_1'),
            ('A_3', 'A_3'),
        ]
    }
    
    
    nodesX_Y_Z = {
        'nodes': {
            'X': {},
            'Y': {},
            'Z': {},
        },
        'edges': [
            ('X', 'Y'),
            ('Z', 'X'),
            ('Z', 'Z'),
        ]
    }
    
    dot.generate_file("threeNodesWithLabelsDirected", threeNodesWithLabelsDirected, with_answers=True)
    dot.generate_file("threeNodesDirected", threeNodesDirected, with_answers=True)
    dot.generate_file("nodesA_1_A_2_A_3", nodesA_1_A_2_A_3, with_answers=True)
    dot.generate_file("nodesX_Y_Z", nodesX_Y_Z, with_answers=True)
    