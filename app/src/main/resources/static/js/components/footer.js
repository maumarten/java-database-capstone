function renderFooter() {
  const footer = document.getElementById("footer");
  if (!footer) return;

  footer.innerHTML = `
    <footer class="footer">
      <div class="footer-inner">
        <div class="footer-logo">
          <img src="/assets/images/logo/logo.png" alt="Smart Clinic" width="40" height="40" />
          <p>© ${new Date().getFullYear()} Smart Clinic</p>
        </div>
        <div class="footer-columns">
          <div class="footer-column">
            <h4>Company</h4>
            <a href="#">About</a><a href="#">Careers</a><a href="#">Press</a>
          </div>
          <div class="footer-column">
            <h4>Support</h4>
            <a href="#">Account</a><a href="#">Help Center</a><a href="#">Contact</a>
          </div>
          <div class="footer-column">
            <h4>Legals</h4>
            <a href="#">Terms</a><a href="#">Privacy</a><a href="#">Licensing</a>
          </div>
        </div>
      </div>
    </footer>
  `;
}
renderFooter();
