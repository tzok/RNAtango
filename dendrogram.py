import numpy as np

from scipy.cluster.hierarchy import dendrogram, linkage
from scipy.spatial.distance import squareform
import sys
import matplotlib.pyplot as plt

if __name__=="__main__":
    calc = np.array([i for i in sys.argv[1].split(',')]).astype(np.float16)
    labels = sys.argv[2].split(',')
    calc = calc.reshape((len(labels),len(labels)))
    dists = squareform(calc)
    linkage_matrix = linkage(dists, "single")
    dendrogram(linkage_matrix, orientation="right",labels=sys.argv[2].split(','))
    plt.box(False)
    plt.savefig(sys.stdout,format="svg" ,bbox_inches='tight')
