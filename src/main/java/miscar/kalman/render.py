import numpy as np
import matplotlib.pyplot as plt
from matplotlib.patches import Ellipse

# Your covariance matrix
P = np.array(
    [
        [0.23973469474852932, 0.04670635949361696, 0.05458655144196689],
        [0.046706359493617074, 0.18834976991870053, 0.07144524297846308],
        [0.054586551441966925, 0.07144524297846307, 0.05953771757472259],
    ]
)

# Mean position
mean = np.array([0, 0])

# Take X/Y part of covariance
cov = P[:2, :2]

# Generate random samples from the Gaussian
samples = np.random.multivariate_normal(mean, cov, 100000)

# -------------------------
# Calculate ellipse
# -------------------------

# Eigenvalues and eigenvectors
eigenvalues, eigenvectors = np.linalg.eigh(cov)

# Sort largest eigenvalue first
order = eigenvalues.argsort()[::-1]
eigenvalues = eigenvalues[order]
eigenvectors = eigenvectors[:, order]

# Angle of ellipse
angle = np.degrees(np.arctan2(eigenvectors[1, 0], eigenvectors[0, 0]))

# 1-sigma ellipse
sigma = 1

width = 2 * sigma * np.sqrt(eigenvalues[0])
height = 2 * sigma * np.sqrt(eigenvalues[1])

ellipse = Ellipse(
    xy=mean,
    width=width,
    height=height,
    angle=angle,
    fill=False,
    edgecolor="red",
    linewidth=2,
)

# -------------------------
# Plot
# -------------------------

fig, ax = plt.subplots(figsize=(8, 6))

h = ax.hist2d(samples[:, 0], samples[:, 1], bins=150, cmap="viridis")

# Add ellipse
ax.add_patch(ellipse)

ax.scatter(mean[0], mean[1], color="red", s=30)

ax.set_xlabel("X")
ax.set_ylabel("Y")
ax.set_title("Covariance Gaussian")

ax.set_aspect("equal")
fig.colorbar(h[3], ax=ax, label="Density")

plt.show()
