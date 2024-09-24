import re
import os
from pprint import pprint


dot_files = []

dir = "/Users/jonisalazar/School/Fall 2024/CSE464/CSE-464-2024-jsalaz59/src/test/resources/DOT/valid"
for test_dir in os.listdir(dir):
    for file in os.listdir(os.path.join(dir, test_dir)):
        if file.endswith(".dot"):
            dot_files += [os.path.join(dir, test_dir, file)]
            
for dot_file in dot_files:
    if "funny" not in dot_file: continue
    test_files = [
        dot_file.replace(".dot", ".edges.txt"),
        dot_file.replace(".dot", ".node_labels.txt"),
        dot_file.replace(".dot", ".nodes.txt"),
    ]
    
    for test_file in test_files:
        with open(test_file, "r") as file:
            print(f'reading file: {os.path.basename(file.name)}')
            while True:
                line = file.readline()[:-1]
                if not line: break
                
                delimiter = ", "
                parts = line.split(delimiter)
                answer = set([parts[0], delimiter.join(parts[1:])])
                print(answer)
        