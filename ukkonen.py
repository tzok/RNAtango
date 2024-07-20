from dataclasses import dataclass
from typing import Dict, List, Set
import sys
import time


class Node:
    index: int
    edges: Dict

    def __init__(self, index: int) -> None:
        self.index = index
        self.edges = dict({})
        # self.links = []


class Tree(Node):
    edges: Dict[int, Set[int]]
    text_stack: List[str]

    active_node: int
    index = -1

    t_time = 0
    find_time = 0
    split_time = 0

    edge_in_label: Dict[int, str] = {}
    leaves_indexes: Set[int] = set()

    def __str__(self) -> str:
        return (
            str(self.index)
            + "_"
            + str(
                [
                    ([f"{i}->[{z}]{str(self.edge_in_label[z])}" for z in x])
                    for i, x in self.edges.items()
                ]
            )
        )

    def add_letter_to_leafs(self, c):
        for label in self.leaves_indexes:
            self.edge_in_label[label] += c

    def add_edge(self, index: int, label: str, inN: int):
        self.edge_in_label[index] = label
        self.edges[index] = set()
        self.edges[inN].add(index)

    def split_edge_at(
        self, new_index: int, label: str, newc: str, from_index, curr_index
    ):
        if curr_index == 0:
            self.add_edge(new_index, newc, from_index)
            self.leaves_indexes.add(new_index)
            return new_index + 1
        elif label == self.edge_in_label[curr_index]:
            self.add_edge(new_index, newc, curr_index)
            self.edges[curr_index].add(new_index)
            self.leaves_indexes.add(new_index)
            return new_index + 1
        else:
            self.add_edge(new_index, label, from_index)
            self.edges[from_index].remove(curr_index)
            self.edges[new_index].add(curr_index)
            self.edge_in_label[curr_index] = self.edge_in_label[curr_index][
                len(label) :
            ]
            self.add_edge(new_index + 1, newc, new_index)
            self.leaves_indexes.add(new_index + 1)
            return new_index + 2

    def find_edge(self, text, index):
        if text == "":
            return [index], False, 0
        for following_index in list(self.edges[index]):
            if self.edge_in_label[following_index].startswith(text):
                if self.edge_in_label[following_index] != text:
                    return ([following_index], True, 0)
                return ([following_index], True, 0)
            elif text.startswith(self.edge_in_label[following_index]):
                e = self.find_edge(
                    text[len(self.edge_in_label[following_index]) :], following_index
                )
                return (
                    e[0] + [following_index],
                    e[1],
                    e[2] + len(self.edge_in_label[following_index]),
                )
        return [], False, 0

    def __init__(self) -> None:
        self.edges = dict()
        self.edges[0] = set()
        self.active_edge = 0
        self.active_node = 0
        self.text_stack = []

    def wait_unload_buffer(self, index):
        t = time.time()
        path = self.find_edge(
            "".join(self.text_stack[self.active_edge :]), self.active_node
        )
        self.find_time += time.time() - t
        if len(self.text_stack) > 0 and (path[1] == False):
            t = time.time()
            edge = self.find_edge(
                "".join(self.text_stack[self.active_edge : -1]), self.active_node
            )
            self.find_time += time.time() - t
            t = time.time()
            index = self.split_edge_at(
                index,
                "".join(self.text_stack[edge[2] : -1]),
                self.text_stack[-1],
                edge[0][1] if len(edge[0]) > 1 else self.active_node,
                edge[0][0],
            )
            self.split_time += time.time() - t
            self.active_edge = 0
            self.active_node = 0
            self.text_stack = self.text_stack[1:]
            return self.wait_unload_buffer(index)
        else:
            self.active_node = 0  # path[0][1] if len(path[0]) > 1 else 0
            self.active_edge = 0  # self.active_edge + 1 if len(path[0]) > 1 else 0
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
        sss = """UUGGCUACuAUGCCAGCUGGUGGAUUGCUCGGCUCAGGCGCUGAUGAAGGACGUGCCAAGCUGCGAUAAGCCAUGGGGAGCCGCACGGAGGCGAAGAACCAUGGAUUUCCGAAUGAGAAUCUCUCUAACAAUUGCUUCGCGCAAUGAGGAACCCCGAGAACUGAAACAUCUCAGUAUCGGGAGGAACAGAAAACGCAAUGUGAUGUCGUUAGUAACCGCGAGUGAACGCGAUACAGCCCAAACCGAAGCCCUCACGGGCAAUGUGGUGUCAGGGCUACCUCUCAUCAGCCGACCGUCUCGACGAAGUCUCUUGGAACAGAGCGUGAUACAGGGUGACAACCCCGUACUCGAGACCAGUACGACGUGCGGUAGUGCCAGAGUAGCGGGGGUUGGAUAUCCCUCGCGAAUAACGCAGGCAUCGACUGCGAAGGCUAAACACAACCUGAGACCGAUAGUGAACAAGUAGUGUGAACGAACGCUGCAAAGUACCCUCAGAAGGGAGGCGAAAUAGAGCAUGAAAUCAGUUGGCGAUCGAGCGACAGGGCAUACAAGGUCCCUCGACGAAUGACCGACGCGCGAGCGUCCAGUAAGACUCACGGGAAGCCGAUGUUCUGUCGUACGUUUUGAAAAACGAGCCAGGGAGUGUGUCUGCAUGGCAAGUCUAACCGGAGUAUCCGGGGAGGCACAGGGAAACCGACAUGGCCGCAGGGCUUUGCCCGAGGGCCGCCGUCUUCAAGGGCGGGGAGCCAUGUGGACACGACCCGAAUCCGGACGAUCUACGCAUGGACAAGAUGAAGCGUGCCGAAAGGCACGUGGAAGUCUGUUAGAGUUGGUGUCCUACAAUACCCUCUCGUGAUCUAUGUGUAGGGGUGAAAGGCCCAUCGAGUCCGGCAACAGCUGGUUCCAAUCGAAACAUGUCGAAGCAUGACCUCCGCCGAGGUAGUCUGUGAGGUAGAGCGACCGAUUGGUGUGUCCGCCUCCGAGAGGAGUCGGCACACCUGUCAAACUCCAAACUUACAGACGCCGUUUGACGCGGGGAUUCCGGUGCGCGGGGUAAGCCUGUGUACCAGGAGGGGAACAACCCAGAGAUAGGUUAAGGUCCCCAAGUGUGGAUUAAGUGUAAUCCUCUGAAGGUGGUCUCGAGCCCUAGACAGCCGGGAGGUGAGCUUAGAAGCAGCUACCCUCUAAGAAAAGCGUAACAGCUUACCGGCCGAGGUUUGAGGCGCCCAAAAUGAUCGGGACUCAAAUCCACCACCGAGACCUGUCCGUACCACUCAUACUGGUAAUCGAGUAGAUUGGCGCUCUAAUUGGAUGGAAGUAGGGGUGAAAACUCCUAUGGACCGAUUAGUGACGAAAAUCCUGGCCAUAGUAGCAGCGAUAGUCGGGUGAGAACCCCGACGGCCUAAUGGAUAAGGGUUCCUCAGCACUGCUGAUCAGCUGAGGGUUAGCCGGUCCUAAGUCAUACCGCAACUCGACUAUGACGAAAUGGGAAACGGGUUAAUAUUCCCGUGCCACUAUGCAGUGAAAGUUGACGCCCUGGGGUCGAUCACGCUGGGCAUUCGCCCAGUCGAACCGUCCAACUCCGUGGAAGCCGUAAUGGCAGGAAGCGGACGAACGGCGGCAUAGGGAAACGUGAUUCAACCUGGGGCCCAUGAAAAGACGAGCAUAGUGUCCGUACCGAGAACCGACACAGGUGUCCAUGGCGGCGAAAGCCAAGGCCUGUCGGGAGCAACCAACGUUAGGGAAUUCGGCAAGUUAGUCCCGUACCUUCGGAAGAAGGGAUGCCUGCUCCGGAACGGAGCAGGUCGCAGUGACUCGGAAGCUCGGACUGUCUAGUAACAACAUAGGUGACCGCAAAUCCGCAAGGACUCGUACGGUCACUGAAUCCUGCCCAGUGCAGGUAUCUGAACACCUCGUACAAGAGGACGAAGGACCUGUCAACGGCGGGGGUAACUAUGACCCUCUUAAGGUAGCGUAGUACCUUGCCGCAUCAGUAGCGGCUUGCAUGAAUGGAUUAACCAGAGCUUCACUGUCCCAACGUUGGGCCCGGUGAACUGUACAUUCCAGUGCGGAGUCUGGAGACACCCAGGGGGAAGCGAAGACCCUAUGGAGCUUUACUGCAGGCUGUCGCUGAGACGUGGUCGCCGAUGUGCAGCAUAGGUAGGAGACACUACACAGGUACCCGCGCUAGCGGGCCACCGAGUCAACAGUGAAAUACUACCCGUCGGUGACUGCGACUCUCACUCCGGGAGGAGGACACCGAUAGCCGGGCAGUUUGACUGGGGCGGUACGCGCUCGAAAAGAUAUCGAGCGCGCCCUAUGGCUAUCUCAGCCGGGACAGAGACCCGGCGAAGAGUGCAAGAGCAAAAGAUAGCUUGACAGUGUUCUUCCCAACGAGGAACGCUGACGCGAAAGCGUGGUCUAGCGAACCAAUUAGCCUGCUUGAUGCGGGCAAUUGAUGACAGAAAAGCUACCCUAGGGAUAACAGAGUCGUCACUCGCAAGAGCACAUAUCGACCGAGUGGCUUGCUACCUCGAUGUCGGUUCCCUCCAUCCUGCCCGUGCAGAAGCGGGCAAGGGUGAGGUUGUUCGCCUAUUAAAGGAGGUCGUGAGCUGGGUUUAGACCGUCGUGAGACAGGUCGGCUGCUAUCUACUGGGUGUGUAAUGGUGUCUGACAAGAACGACCGUAUAGUACGAGAGGAACUACGGUUGGUGGCCACUGGUGUACCGGUUGUUCGAGAGAGCACGUGCCGGGUAGCCACGCCACACGGGGUAAGAGCUGAACGCAUCUAAGCUCGAAACCCACUUGGAAAAGAGACACCGCCGAGGUCCCGCGUACAAGACGCGGUCGAUAGACUCGGGGUGUGCGCGUCGAGGUAACGAGACGUUAAGCCCACGAGCACUAACAGACCAAAGCCAUCAU"""
        start = time.time()
        t.build_index(f"{sys.argv[1]}$", True)
        end = time.time()
        print(end - start)
        print(t.t_time)
        print(t.split_time)
        print(t.find_time)
        print(sss[222:225])
        aaaa = t.find_edge(sss[222:225], 0)[0][::-1]
        for ind in aaaa:
            print(t.edge_in_label[ind])
        print(sss[aaaa[-1] - 3 : aaaa[-1]])

    else:
        t.build_index("mississippi$", True)
        print(t)
        assert (
            str(t)
            == """-1_['s->4_["si->8_[\\'ssippi$->3_[]\\', \\'ppi$->9_[]\\']", "i->10_[\\'ssippi$->5_[]\\', \\'ppi$->11_[]\\']"]', 'i->12_["ssi->6_[\\'ssippi$->2_[]\\', \\'ppi$->7_[]\\']", \\'ppi$->13_[]\\', \\'$->17_[]\\']', "p->15_['pi$->14_[]', 'i$->16_[]']", 'mississippi$->1_[]', '$->18_[]']"""
        )
