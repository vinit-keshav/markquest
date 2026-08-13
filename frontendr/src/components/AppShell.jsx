import AppFooter from "./AppFooter";
import AppHeader from "./AppHeader";

function AppShell({ children }) {
  return (
    <div className="app-shell">
      <AppHeader />
      {children}
      <AppFooter />
    </div>
  );
}

export default AppShell;
