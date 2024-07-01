from dataclasses import dataclass
from typing import Dict, List
import sys
import time


class Node:
    index: int
    edges: Dict

    def __init__(self, index: int) -> None:
        self.index = index
        self.edges = dict({})
        # self.links = []

    def add_letter_to_leafs(self, c) -> bool:
        if len(self.edges.keys()) == 0:
            return True
        for label in list(self.edges.keys()):
            if self.edges[label].add_letter_to_leafs(c):
                self.edges[label + c] = self.edges.pop(label)
        return False

    def add_edge(self, index: int, label: str):
        self.edges[label] = Node(index)

    def split_edge_at(self, index: int, label: str, newc: str, position):
        if len(position) == 0:
            self.add_edge(index, newc)
            return index + 1
        elif len(position) == 1:
            self.edges[label] = Node(index)
            self.edges[label].edges[position[0][len(label) :]] = self.edges[position[0]]
            self.edges[label].add_edge(index + 1, newc)
            del self.edges[position[0]]
            return index + 2
        elif len(position) == 2:
            return self.edges[position[0]].split_edge_at(
                index, label[len(position[0]) :], newc, position[1]
            )

    def find_edge(self, text, memory):
        if memory is not None and len(memory) == 2:
            e = self.edges[memory[0]].find_edge(text[len(memory[0]) :], memory[1])
            return [memory[0], e[0]], e[1]

        if text == "":
            return [], False
        for key, edge in list(self.edges.items()):
            if key.startswith(text):
                if key != text:
                    return [key], self.index
                return [key, []], self.index
            elif text.startswith(key):
                e = edge.find_edge(text[len(key) :], None)
                return [key, e[0]], e[1]
        return [], False

    def __str__(self) -> str:
        return (
            str(self.index)
            + "_"
            + str([f"{i}->{str(x)}" for i, x in self.edges.items()])
        )


class Tree(Node):
    edges: Dict[str, Node]
    text_stack: List[str]

    active_node: int
    index = -1

    t_time = 0

    def __init__(self) -> None:
        self.edges = dict()
        self.active_edge = None
        self.text_stack = []

    def wait_unload_buffer(self, index):
        path = self.find_edge("".join(self.text_stack), self.active_edge)
        if len(self.text_stack) > 0 and (path[1] == False):
            edge = self.find_edge("".join(self.text_stack[:-1]), self.active_edge)
            index = self.split_edge_at(
                index, "".join(self.text_stack[:-1]), self.text_stack[-1], edge[0]
            )
            self.active_edge = None
            self.active_node = edge[1]
            self.text_stack = self.text_stack[1:]
            return self.wait_unload_buffer(index)
        self.active_edge = path[0]
        return index

    def build_index(self, text, target: bool):
        ind = 1
        for i, c in enumerate(text):
            self.text_stack.append(c)
            t = time.time()
            self.add_letter_to_leafs(c)
            self.t_time += time.time() - t
            ind = self.wait_unload_buffer(ind)


if __name__ == "__main__":
    t = Tree()
    if len(sys.argv) > 1:
        start = time.time()
        t.build_index(f"{sys.argv[1]}$", True)
        end = time.time()
        # print(t)
        print(end - start)
        print(t.t_time)
    else:
        t.build_index("mississippi$")
        assert (
            str(t)
            == """-1_['s->4_["si->8_[\\'ssippi$->3_[]\\', \\'ppi$->9_[]\\']", "i->10_[\\'ssippi$->5_[]\\', \\'ppi$->11_[]\\']"]', 'i->12_["ssi->6_[\\'ssippi$->2_[]\\', \\'ppi$->7_[]\\']", \\'ppi$->13_[]\\', \\'$->17_[]\\']', "p->15_['pi$->14_[]', 'i$->16_[]']", 'mississippi$->1_[]', '$->18_[]']"""
        )
