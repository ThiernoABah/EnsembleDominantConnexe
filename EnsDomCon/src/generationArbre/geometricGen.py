import networkx as nx
import random
n = 1000
G = nx.random_geometric_graph(n, 54.0)

f= open("../../inputPy.points","w")
pos=nx.get_node_attributes(G,'pos')
for v in pos.values():
  x = int(80+v[0]*1080)
  y = int(v[1]*1000)
  if(y>=900):
    y= y - random.randint(100,800)
  s = str(x )+" "+str(y)+"\n"
  f.write(s)
f.close()
