export function getStarsIcon(averageRating: number) {
  const stars = [];
  const fullStar = '<i class="bi bi-star-fill" style="color: gold;"></i>';
  const halfStar = '<i class="bi bi-star-half" style="color: gold;"></i>';
  const emptyStar = '<i class="bi bi-star" style="color: gray;"></i>';

  for (let i = 0; i < Math.floor(averageRating); i++) {
    stars.push(fullStar);
  }

  if (averageRating % 1 !== 0) {
    stars.push(halfStar);
  }

  for (let i = 0; i < 5 - Math.ceil(averageRating); i++) {
    stars.push(emptyStar);
  }
  return stars;
}
