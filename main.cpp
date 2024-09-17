#include <QApplication>
#include <QWidget>
#include <QLabel>
#include <QPushButton>
#include <QVBoxLayout>
#include <QPixmap>

int main(int argc, char *argv[]) {
    QApplication app(argc, argv);

    QWidget window;
    window.resize(400, 300);
    window.setWindowTitle("Bruce app");

    window.setStyleSheet("background-color: #000000;");

    QVBoxLayout *layout = new QVBoxLayout;

    QLabel *imageLabel = new QLabel;
    QPixmap imagePixmap("assets/bruce_menu.jpg");
    imageLabel->setPixmap(imagePixmap.scaled(400, 200, Qt::KeepAspectRatio));

    QLabel *textLabel = new QLabel("Bruce app");
    textLabel->setAlignment(Qt::AlignCenter);

    QPushButton *updateButton = new QPushButton("Update firmware");
    QPushButton *serialButton = new QPushButton("Serial");

    updateButton->setStyleSheet("color: purple;background-color: gray;");
    serialButton->setStyleSheet("color: purple;background-color: gray;");

    layout->addWidget(imageLabel);
    layout->addWidget(textLabel);
    layout->addWidget(updateButton);
    layout->addWidget(serialButton);

    window.setLayout(layout);

    window.show();
    return app.exec();
}

