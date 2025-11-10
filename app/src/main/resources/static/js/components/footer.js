function renderFooter() {
  const footer = document.getElementById("footer");
  if (!footer) return;

  footer.innerHTML = `
    <footer class="footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <img src="/assets/images/logo/logo.png" alt="Smart Clinic" class="footer-brand-logo" width="44" height="44" />
          <div class="brand-text">
            <strong>Smart Clinic</strong>
            <small>Better scheduling for better care.</small>
          </div>
        </div>
        <div class="footer-columns">
          <div class="footer-column">
            <h4>Company</h4>
            <a href="#">About</a>
            <a href="#">Careers</a>
            <a href="#">Press</a>
          </div>
          <div class="footer-column">
            <h4>Support</h4>
            <a href="#">Account</a>
            <a href="#">Help Center</a>
            <a href="#">Contact</a>
          </div>
          <div class="footer-column">
            <h4>Legals</h4>
            <a href="#">Terms</a>
            <a href="#">Privacy</a>
            <a href="#">Licensing</a>
          </div>
        </div>
        <div class="footer-bottom">
          <span>© ${new Date().getFullYear()} Smart Clinic. All rights reserved.</span>
          <nav class="footer-bottom-links">
            <a href="#">Status</a>
            <a href="#">Docs</a>
            <a href="#">Security</a>
          </nav>
        </div>
      </div>
    </footer>
  `;
}
renderFooter();
