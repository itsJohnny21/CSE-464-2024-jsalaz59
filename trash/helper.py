from itertools import chain, combinations
import shutil
import subprocess
import string
import random
import os
from pprint import pprint
        
class DOTGenerator():
    def __init__(self, directory:str):
        self.directory = directory
    
    def generate_id(self, length:int = 3, options:str = "a") -> str:
        if length < 3:
            raise Exception(f"minimum ID length is {3}")

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
    
    def delete_files(self, filename:str):
        if os.path.exists(self.directory):
            valid_path = os.path.join(self.directory, "valid")
            for file in os.listdir(valid_path):
                if file.startswith(filename):
                    os.remove(os.path.join(valid_path, file))
                    
            invalid_path = os.path.join(self.directory, "invalid")
            for file in os.listdir():
                if file.startswith(filename):
                    os.remove(os.path.join(invalid_path, file))
    
    def generate_random_str(self, options:str="a5") -> str:
        n_str = ''.join(filter(str.isdigit, options))
        if n_str:
            n = int(n_str)
        else:
            n = random.randint(1, 10)
            

        if 'l' in options:
            char_set = string.ascii_lowercase
        elif 'u' in options:
            char_set = string.ascii_uppercase
        elif 'a' in options:
            char_set = string.ascii_letters
            
        if 'n' in options:
            char_set += string.digits
            
        return ''.join(random.choice(char_set) for _ in range(n))
    
    def format_tuple(self, t):
        return f"({t[0]},{t[1]})"
    
    def generate_graph(self, graph_options:str="d", node_options:str="a10", edge_options:str="l6"):
        graph = {
                'type': DIGRAPH if 'd' in graph_options else GRAPH,
                'name': self.generate_random_str(options="4u") if 'r' in graph_options else "",
                'nodes': {},
                'edges': {}
            }
            
        num_nodes_str = ''.join(filter(str.isdigit, node_options))
        if num_nodes_str:
            num_nodes = int(num_nodes_str)
        else:
            num_nodes = random.randint(1, 10)
            
        while len(graph['nodes']) < num_nodes:
            node = self.generate_id(length=max(3, num_nodes / (len(string.ascii_letters) * len(string.digits))), options=node_options)
            
            graph['nodes'][node] = {}
            
            if 'l' in node_options:
                label = self.generate_random_str(options="5a")
                graph['nodes'][node]['label'] = label
        
        num_edges_str = ''.join(filter(str.isdigit, edge_options))
        if num_edges_str:
            num_edges = int(num_edges_str)
        else:
            num_edges = min(num_nodes**2, random.randint(1, 10))
            
        while len(graph['edges']) < num_edges:
            to_node = random.choice(list(graph['nodes']))
            from_node = random.choice(list(graph['nodes']))
            edge = (from_node, to_node)
            graph['edges'][edge] = {}
            
            if 'l' in edge_options or 'w' in edge_options:
                attrs = []
                
                if 'l' in edge_options:
                    label = f'{from_node} to {to_node}'
                    graph['edges'][edge]['label'] = label
            
                if 'w' in edge_options:
                    weight = random.randint(0, num_nodes)
                    graph['edges'][edge]['weight'] = weight
                    
        return graph
            
        
    def generate_file(self, filename:str="", graph:dict=None, graph_options:str="d", node_options:str="a10", edge_options:str="l6", error_options="", with_answers:bool=False, invalid=False) -> None:
        if not invalid:
            error_options = ""
            
        if not graph:
            graph = self.generate_graph(graph_options=graph_options, node_options=node_options, edge_options=edge_options)
            
        if graph['type'] == GRAPH:
            invalid = True
            
        if 'n' in error_options:
            graph['nodes']["1!"] = {}
            
        if 'u' in error_options:
            graph['type'] = GRAPH
            
        nodes_section_parts = []
        for node, attrs in graph['nodes'].items():
            attrs_parts = []
            
            for attr, attr_value in attrs.items():
                if not attr_value: continue
                attrs_parts += [f'{attr}="{attr_value}"']
                
            node_str = f'\t{node} [{" ".join(attrs_parts)}];'
            
            if 'b' in error_options:
                node_str = node_str.replace(random.choice(["]", "["]), "")
            
            nodes_section_parts += [node_str]
            
        nodes_section = "\n".join(nodes_section_parts)
            
        edges_section_parts = []
        for edge, attrs in graph['edges'].items():
            from_node, to_node = edge
            
            if 'e' in error_options:
                link = random.choice([">", "-->"])
            else :
                link = EDGE_TYPE[graph["type"]]
            
            edge_str = f'\t{from_node} {link} {to_node}'
            attrs_parts = []
            
            for attr, attr_value in attrs.items():
                if not attr_value: continue
                attrs_parts += [f'{attr}="{attr_value}"']
                
            edge_str += f' [{" ".join(attrs_parts)}];'
            edges_section_parts += [edge_str]
            
        edges_section = "\n".join(edges_section_parts)
        
        if edges_section == "" and nodes_section == "":
            dot_content = f'''{graph["type"]} {{\n}}'''
        else:
            dot_content =  f'''{graph["type"]} {{\n{nodes_section}\n\n{edges_section}\n}}'''
            
        if 's' in error_options:
            dot_content += ";"
            
        if 't' in error_options:
            dot_content = "breh" + dot_content
            
        if not invalid:
            process = subprocess.Popen(
                ["dot"], 
                stdin=subprocess.PIPE, 
                stdout=subprocess.PIPE, 
                stderr=subprocess.PIPE
            )

            stderr = process.communicate(input=dot_content.encode())

            if process.returncode != 0:
                raise Exception(f'invalid DOT graph: {stderr[1].decode()}')
            
        if not filename:
            filename = f'{graph_options}-{node_options}'
            
        valid_path = os.path.join(self.directory, "valid")
        invalid_path = os.path.join(self.directory, "invalid")
        
        if not invalid:
            if not os.path.exists(valid_path): os.mkdir(valid_path) 
            sub_directory = os.path.join(valid_path, filename)
            if not os.path.exists(sub_directory): os.mkdir(sub_directory) 
                   
            with open(f'{os.path.join(valid_path, sub_directory, filename)}.dot', "w") as file:
                file.write(dot_content)
                
            if with_answers and not invalid:
                edges = "\n".join([self.format_tuple(edge) for edge in graph['edges']]) + "\n"
                nodes = "\n".join([str(node) for node in graph['nodes']]) + "\n"
                node_labels = "\n".join(f'{self.format_tuple((node, graph["nodes"][node]["label"] if "label" in graph["nodes"][node] else ""))}' for node in graph['nodes']) + "\n"
                edge_labels = "\n".join(f'{self.format_tuple((self.format_tuple(edge), graph["edges"][edge]["label"] if "label" in graph["edges"][edge] else ""))}' for edge in graph['edges']) + "\n"
                edge_weights = "\n".join(f'{self.format_tuple((self.format_tuple(edge), graph["edges"][edge]["weight"] if "weight" in graph["edges"][edge] else ""))}' for edge in graph['edges']) + "\n"
                
                answers = {
                    'edges': edges,
                    'nodes': nodes,
                    'node_labels': node_labels,
                    'edge_weights': edge_weights,
                    'edge_labels': edge_labels,
                }
                
                for category in answers:
                    with open(f'{os.path.join(valid_path, sub_directory, filename)}.{category}.txt', "w") as file:
                        file.write(answers[category])
                        
        else:
            if not os.path.exists(invalid_path): os.mkdir(invalid_path)
            sub_directory = os.path.join(invalid_path, filename)
            if not os.path.exists(sub_directory): os.mkdir(sub_directory) 
            with open(f'{os.path.join(invalid_path, sub_directory, filename)}.dot', "w") as file:
                file.write(dot_content)
            

DIGRAPH = 'digraph'
GRAPH = 'graph'

EDGE_TYPE = {
    DIGRAPH: "->",
    GRAPH: "--"
}

nodesX_Y_Z = {
    'type': DIGRAPH,
    'nodes': {
        'X': {},
        'Y': {},
        'Z': {},
    },
    'edges': {
        ('X', 'Y'): {},
        ('Z', 'X'): {},
        ('Z', 'Z'): {},
    },
}

emptyGraph = {
    'type': DIGRAPH,
    'nodes': {},
    'edges': {},
}

someNodesZeroEdges = {
    'type': DIGRAPH,
    'nodes': {
        'A': {},
        'B': {},
        'C': {},
    },
    'edges': {},
}

zeroNodesSomeEdges = {
    'type': DIGRAPH,
    'nodes': {},
    'edges': {
        ('A', 'B'): {},
        ('B', 'C'): {},
    },
}

nodeIDsWithLettersOnly = {
    'type': DIGRAPH,
    'nodes': {
        'A': {},
        'B': {},
        'C': {},
        'D': {},
    },
    'edges': {
        ('A', 'B'): {},
        ('C', 'D'): {},
    },
}

nodeIDsWithNumbers = {
    'type': DIGRAPH,
    'nodes': {
        'Node1': {},
        'Node2': {},
        'Node3': {},
    },
    'edges': {
        ('Node1', 'Node2'): {},
        ('Node2', 'Node3'): {},
    },
}

nodeIDsWithUnderscores = {
    'type': DIGRAPH,
    'nodes': {
        'Node_A': {},
        'Node_B': {},
        'Node_C': {},
    },
    'edges': {
        ('Node_A', 'Node_B'): {},
        ('Node_B', 'Node_C'): {},
    },
}

nodesX_Y_Z = {
    'type': DIGRAPH,
    'nodes': {
        'X': {},
        'Y': {},
        'Z': {},
    },
    'edges': {
        ('X', 'Y'): {},
        ('Z', 'X'): {},
        ('Z', 'Z'): {},
    },
}

nodesX_Y_ZLabeled = {
    'type': DIGRAPH,
    'nodes': {
        'X': {'label': 'Node X'},
        'Y': {'label': 'Node Y'},
        'Z': {'label': 'Node Z'},
    },
    'edges': {
        ('X', 'Y'): {'label': 'Edge XY'},
        ('Z', 'X'): {'label': 'Edge ZX'},
        ('Z', 'Z'): {'label': 'Edge ZZ'},
    },
}

threeNodes = {
    'type': DIGRAPH,
    'nodes': {
        'A': {},
        'B': {},
        'C': {},
    },
    'edges': {},
}

fourEdges = {
    'type': DIGRAPH,
    'nodes': {
        'A': {},
        'B': {},
        'C': {},
        'D': {},
    },
    'edges': {
        ('A', 'B'): {},
        ('B', 'C'): {},
        ('C', 'D'): {},
        ('D', 'A'): {},
    },
}

circularABC = {
    'type': DIGRAPH,
    'nodes': {
        'A': {},
        'B': {},
        'C': {},
    },
    'edges': {
        ('A', 'B'): {},
        ('B', 'C'): {},
        ('C', 'A'): {},
    },
}

invalidNodeID = {
    'type': DIGRAPH,
    'nodes': {
        'A': {},
        'B': {},
        '2A': {},
    },
    'edges': {
        ('A', 'B'): {},
        ('B', '2A'): {},
        ('2A', '2A'): {},
    },
}

funnyGraph = {
    'type': DIGRAPH,
    'nodes': {
        'W': {
            'label': '',
        },
        'X': {
            'label': '',
        },
        'Y': {
            'label': '',
        },
        'Z': {
            'label': '',
        },
    },
    'edges': {
        ('X', 'Y'): {
            'label': 'what',
            'weight': 3,
        },
        ('Z', 'X'): {
            'label': 'did the baby say',
            'weight': 2,
        },
        ('Z', 'Z'): {
            'label': 'to his computer science father',
            'weight': 1,
        },
        ('W', 'Z'): {
            'label': '<data>',
            'weight': 1,
        }
    },
}


def main():
    tmp_path = "/Users/jonisalazar/School/Fall 2024/CSE464/CSE-464-2024-jsalaz59/src/test/resources/DOT"
    if not os.path.exists(tmp_path): os.mkdir(tmp_path)
    dot = DOTGenerator(tmp_path)
    
    # # # Valid
    # dot.generate_file(filename="nodesX_Y_Z", graph=nodesX_Y_Z, with_answers=True)
    # dot.generate_file(filename="emptyGraph", graph=emptyGraph, with_answers=True)
    # dot.generate_file(filename="someNodesZeroEdges", graph=someNodesZeroEdges, with_answers=True)
    # dot.generate_file(filename="zeroNodesSomeEdges", graph=zeroNodesSomeEdges, with_answers=True)
    # dot.generate_file(filename="nodeIDsWithLettersOnly", graph=nodeIDsWithLettersOnly, with_answers=True)
    # dot.generate_file(filename="nodeIDsWithNumbers", graph=nodeIDsWithNumbers, with_answers=True)
    # dot.generate_file(filename="nodeIDsWithUnderscores", graph=nodeIDsWithUnderscores, with_answers=True)
    # dot.generate_file(filename="nodesX_Y_ZLabeled", graph=nodesX_Y_ZLabeled, with_answers=True)
    # dot.generate_file(filename="threeNodes", graph=threeNodes, with_answers=True)
    # dot.generate_file(filename="fourEdges", graph=fourEdges, with_answers=True)
    # dot.generate_file(filename="circularABC", graph=circularABC, with_answers=True)
    # dot.generate_file(filename="funnyGraph", graph=funnyGraph, with_answers=True)
    
    # Invalid
    # dot.generate_file(filename="undirectedGraph", graph=nodesX_Y_Z, error_options="u", invalid=True)
    # dot.generate_file(filename="invalidNodeSyntax", graph=fourEdges, error_options="n", invalid=True)
    # dot.generate_file(filename="invalidEdgeSyntax", graph=circularABC, error_options="e", invalid=True)
    # dot.generate_file(filename="invalidSemicolon", graph=funnyGraph, error_options="s", invalid=True)
    # dot.generate_file(filename="graphTypeTypo", graph=fourEdges, error_options="t", invalid=True)
    # dot.generate_file(filename="bracketIncomplete", graph=nodesX_Y_ZLabeled, error_options="b", invalid=True)
    
    # Generated
    dot.generate_file(filename="selfLoopEdges", graph_options="d", node_options="4", edge_options="16", with_answers=True)
    # dot.generate_file(filename="veryLargeGraph", graph_options="d", node_options="999al", edge_options="999l", with_answers=True)
    
    
main()

