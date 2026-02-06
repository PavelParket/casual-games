import { createRoot } from 'react-dom/client';
import { Provider } from 'react-redux';
import './ui/ui.css';
import App from './App.tsx';
import { store } from './store/store.ts';
import { AxiosInterceptorsConfig } from './api/AxiosInterceptorsConfig.ts';

AxiosInterceptorsConfig(store);

createRoot(document.getElementById('root')!).render(
   //<StrictMode>
   <Provider store={store}>
      <App />
   </Provider>
   //</StrictMode>
)
