import subprocess
import os
import time
import numpy as np
import matplotlib.pyplot as plt
from itertools import product
from mpl_toolkits.mplot3d import Axes3D

# ------------------------------
# Parámetros a probar
# ------------------------------
ks = [1, 5, 25, 125]        # valores de k
lambdas = [0.001, 0.01, 0.05, 1]  # valores de lambda
stiters = [100]              # solo graficamos stiter=100

reps = 10                    # número de repeticiones por combinación
result_folder = "resultados"

# Crear carpeta de resultados si no existe
os.makedirs(result_folder, exist_ok=True)

# Diccionario para almacenar resultados
# resultado[(k, lambda, stiter)] = {"media": ..., "std": ...}
resultado = {}

# ------------------------------
# Ejecutar experimentos
# ------------------------------
for k, lam, st in product(ks, lambdas, stiters):
    beneficios = []
    for r in range(reps):
        # Semilla diferente para cada repetición
        seed = int(time.time() * 1000) % 100000 + r

        # Comando Java
        cmd = [
            "java", "-cp", "lib/*;src", "Main", "SA",
            "10000", str(st), str(k), str(lam), str(seed)
        ]
        print(f"Ejecutando: k={k}, lambda={lam}, stiter={st}, rep={r+1}")
        subprocess.run(cmd)

        # Pequeña espera para que los archivos tengan nombre único
        time.sleep(0.2)

        # Leer último archivo generado
        files = sorted([f for f in os.listdir(result_folder) if f.startswith("exp")])
        latest_file = os.path.join(result_folder, files[-1])

        # Extraer beneficio (línea 5 del archivo, saltando comentarios)
        with open(latest_file, "r") as f:
            lines = [l.strip() for l in f.readlines() if not l.startswith("#") and l.strip() != ""]
            beneficio = float(lines[4])
            beneficios.append(beneficio)

    # Guardar media y desviación
    resultado[(k, lam, st)] = {
        "media": np.mean(beneficios),
        "std": np.std(beneficios)
    }

# ------------------------------
# Gráfico 3D de barras para stiter=100
# ------------------------------
k_vals_unique = sorted(ks)
lambda_vals_unique = sorted(lambdas)

fig = plt.figure()
ax = fig.add_subplot(111, projection='3d')

dx = dy = 0.4
xpos, ypos, dz = [], [], []

# Colores por fila (lambda)
colors = ['skyblue', 'lightgreen', 'salmon', 'orange']

# Construir coordenadas y alturas
for i, lam in enumerate(lambda_vals_unique):
    for j, k in enumerate(k_vals_unique):
        xpos.append(j)
        ypos.append(i)
        dz.append(resultado[(k, lam, 100)]["media"])

dz = np.array(dz)

# Normalización relativa
dz_normalized = (dz - dz.min()) / (dz.max() - dz.min())
zpos = np.zeros_like(dz_normalized)


fig = plt.figure()
ax = fig.add_subplot(111, projection='3d')
# Dibujar cada barra con color según fila (lambda)
for i, (x, y, dz_val) in enumerate(zip(xpos, ypos, dz_normalized)):
    color = colors[y % len(colors)]  # color por fila
    ax.bar3d(x, y, 0, dx, dy, dz_val, color=color, shade=True)

# Etiquetas
ax.set_xticks(np.arange(len(k_vals_unique)) + dx/2)
ax.set_xticklabels(k_vals_unique)
ax.set_yticks(np.arange(len(lambda_vals_unique)) + dy/2)
ax.set_yticklabels(lambda_vals_unique)
ax.set_xlabel("k")
ax.set_ylabel("lambda")
ax.set_zlabel("Beneficio normalizado")
ax.set_title("Beneficio medio SA normalizado (stiter=100)")

plt.show()