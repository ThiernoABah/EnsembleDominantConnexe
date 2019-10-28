
import networkx as nx
import random
n = 1000
p = {i: (random.randint(80, 1200), random.randint(80, 900)) for i in range(n)}
G = nx.random_geometric_graph(n, 55.0,pos = p)

f= open("input.points","w")
pos=nx.get_node_attributes(G,'pos')
for v in pos.values():
  s = str(v[0])+" "+str(v[1])+"\n"
  f.write(s)
f.close()
#nx.draw(G)