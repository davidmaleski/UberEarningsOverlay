package com.example.uberearningsoverlay.ui.theme;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

public class UberAccessibilityService extends AccessibilityService {
    public static String distance = null; // Armazenar a distância capturada

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getSource() == null) return;

        AccessibilityNodeInfo node = event.getSource();
        traverseNode(node);
    }

    private void traverseNode(AccessibilityNodeInfo node) {
        if (node == null) return;

        // Procurar por um TextView que contenha "km" e tenha um ID ou classe específica
        if (node.getText() != null && node.getClassName() != null) {
            String text = node.getText().toString();
            String className = node.getClassName().toString();
            // Verificar se é um TextView e contém "km"
            if (className.equals("android.widget.TextView") && text.contains("km")) {
                // Opcional: Adicionar mais condições, como verificar o ID do recurso
                // if (node.getViewIdResourceName() != null && node.getViewIdResourceName().contains("distance_text")) {
                distance = text; // Armazenar a distância
                Toast.makeText(this, "Distância capturada: " + distance, Toast.LENGTH_SHORT).show();
                // }
            }
        }

        // Percorrer os nós filhos
        for (int i = 0; i < node.getChildCount(); i++) {
            traverseNode(node.getChild(i));
        }

        node.recycle();
    }

    @Override
    public void onInterrupt() {
        // Método necessário, mas não usaremos por agora
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;
        info.packageNames = new String[]{"com.ubercab.driver"}; // Pacote do app da Uber Driver
        setServiceInfo(info);
    }
}