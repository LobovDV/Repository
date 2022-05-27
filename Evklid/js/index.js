document.addEventListener('DOMContentLoaded', function() {
  const swiper = new Swiper('.section-hero__swiper', {
    speed: 300,
    pagination: {
      el: '.section-hero__pagination',
      type: 'bullets',
      clickable: true
    },
  });

  document.querySelectorAll('.section-how__step').forEach(function(howLink) {
    howLink.addEventListener('click', function(event) {
      document.querySelectorAll('.section-how__step').forEach(function(hl) {
        hl.classList.remove('active-step');
      })
      howLink.classList.add('active-step');
      const path = event.currentTarget.dataset.path;
      document.querySelectorAll('.section-how__content').forEach(function(howContent) {
        howContent.classList.remove('how-content-active');
      })
      document.querySelector(`[data-target="${path}"]`).classList.add('how-content-active');
    })
  })

  document.querySelector(".header__burger").addEventListener("click", function() {
    document.querySelector(".header__nav").classList.add("active");
  })
  document.querySelector(".header__nav-close").addEventListener("click", function() {
    document.querySelector(".header__nav").classList.remove("active");
  })

  document.querySelector(".header__search-btn").addEventListener("click", function() {
    document.querySelector(".header__form").classList.add("form-active");
  })
  document.querySelector(".header__close-btn").addEventListener("click", function() {
    document.querySelector(".header__form").classList.remove("form-active");
  })
})

